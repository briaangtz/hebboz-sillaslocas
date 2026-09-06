package com.sillaslocas;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class SillasLocasManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(SillasLocasManager.class);

    private final ChairSpawner chairs = new ChairSpawner();
    private final Set<Integer> participants = new LinkedHashSet<Integer>();
    private final Set<Integer> alive = new LinkedHashSet<Integer>();

    private volatile EventState state = EventState.IDLE;
    private volatile int timerGeneration;
    private int round;

    public synchronized EventState getState() {
        return state;
    }

    public synchronized boolean isJoinable() {
        return state == EventState.JOINABLE;
    }

    public synchronized boolean isPlaying() {
        return state == EventState.PLAYING;
    }

    public ChairSpawner chairs() {
        return chairs;
    }

    public void startScheduler() {
        if (!SillasLocasConfig.enabled()) {
            LOGGER.info("[SillasLocas] Automático desactivado (sillaslocas.enabled=0).");
            return;
        }
        scheduleNextCycle(seconds(SillasLocasConfig.startupDelaySeconds()));
        LOGGER.info("[SillasLocas] Primer evento en {} segundos; después cada {} minutos.",
                SillasLocasConfig.startupDelaySeconds(), SillasLocasConfig.intervalMinutes());
    }

    public synchronized void forceStart() {
        cancelTimers();
        beginJoinWindow();
    }

    public synchronized void forceStop() {
        cancelTimers();
        finishEvent(null, false);
        scheduleNextCycle(seconds(SillasLocasConfig.intervalMinutes() * 60));
    }

    public synchronized String statusLine() {
        return "Estado: " + state + " | inscritos: " + participants.size() + " | vivos: " + alive.size() + " | ronda: " + round;
    }

    public synchronized boolean tryJoin(Habbo habbo) {
        if (habbo == null) {
            return false;
        }
        if (!SillasLocasConfig.enabled() && state == EventState.IDLE) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.disabled"));
            return false;
        }
        if (state == EventState.IDLE || state == EventState.ENDING) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.not_active"));
            return false;
        }
        if (state == EventState.PLAYING && !participants.contains(habbo.getHabboInfo().getId())) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.already_playing"));
            return false;
        }
        if (SillasLocasConfig.roomId() <= 0) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.room_missing"));
            return false;
        }
        if (state == EventState.JOINABLE && !participants.contains(habbo.getHabboInfo().getId())
                && participants.size() >= SillasLocasConfig.maxPlayers()) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.full"));
            return false;
        }

        Room room = loadEventRoom();
        if (room == null) {
            Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.room_missing"));
            return false;
        }

        participants.add(habbo.getHabboInfo().getId());
        teleport(habbo, SillasLocasConfig.roomId());
        Broadcasts.whisper(habbo, SillasLocasConfig.msg("sillaslocas.msg.teleport_ok"));
        return true;
    }

    public synchronized void onUserLeft(Habbo habbo, Room room) {
        if (habbo == null || room == null || room.getId() != SillasLocasConfig.roomId()) {
            return;
        }
        int id = habbo.getHabboInfo().getId();
        participants.remove(id);
        boolean wasAlive = alive.remove(id);
        if (state == EventState.PLAYING && wasAlive) {
            checkWinOrContinue();
        }
    }

    private void beginJoinWindow() {
        if (!SillasLocasConfig.enabled() && state == EventState.IDLE) {
            scheduleNextCycle(seconds(SillasLocasConfig.intervalMinutes() * 60));
            return;
        }
        if (SillasLocasConfig.roomId() <= 0) {
            LOGGER.warn("[SillasLocas] Configura sillaslocas.room_id antes de usar el evento.");
            scheduleNextCycle(seconds(SillasLocasConfig.intervalMinutes() * 60));
            return;
        }

        participants.clear();
        alive.clear();
        round = 0;
        state = EventState.JOINABLE;
        loadEventRoom();
        Broadcasts.hotel(SillasLocasConfig.msg("sillaslocas.msg.alert"));
        LOGGER.info("[SillasLocas] Ventana de inscripción abierta.");

        runLater(seconds(SillasLocasConfig.warnSeconds() + SillasLocasConfig.joinSeconds()), new Runnable() {
            @Override
            public void run() {
                startMatch();
            }
        });
    }

    private synchronized void startMatch() {
        if (state != EventState.JOINABLE) {
            return;
        }
        Room room = loadEventRoom();
        syncParticipantsInRoom(room);

        if (participants.size() < SillasLocasConfig.minPlayers()) {
            Broadcasts.hotel(SillasLocasConfig.msg("sillaslocas.msg.cancelled_few"));
            cleanupRoom(room);
            state = EventState.IDLE;
            scheduleNextCycle(seconds(SillasLocasConfig.intervalMinutes() * 60));
            return;
        }

        alive.clear();
        alive.addAll(participants);
        state = EventState.PLAYING;
        round = 0;
        Broadcasts.room(room, SillasLocasConfig.msg("sillaslocas.msg.starting"));
        beginRound();
    }

    private void beginRound() {
        Room room = loadEventRoom();
        if (room == null || state != EventState.PLAYING) {
            return;
        }
        pruneMissing(room);
        if (checkWinOrContinue()) {
            return;
        }

        round++;
        int chairCount = Math.max(1, alive.size() - 1);
        chairs.spawn(room, chairCount);

        String roundMsg = SillasLocasConfig.msg("sillaslocas.msg.round")
                .replace("%round%", String.valueOf(round))
                .replace("%chairs%", String.valueOf(chairCount))
                .replace("%players%", String.valueOf(alive.size()));
        Broadcasts.room(room, roundMsg);
        Broadcasts.room(room, SillasLocasConfig.msg("sillaslocas.msg.music_on"));

        runLater(seconds(SillasLocasConfig.walkSeconds()), new Runnable() {
            @Override
            public void run() {
                sitPhase();
            }
        });
    }

    private synchronized void sitPhase() {
        if (state != EventState.PLAYING) {
            return;
        }
        Room room = loadEventRoom();
        Broadcasts.room(room, SillasLocasConfig.msg("sillaslocas.msg.music_off"));
        runLater(seconds(SillasLocasConfig.sitSeconds()), new Runnable() {
            @Override
            public void run() {
                evaluateRound();
            }
        });
    }

    private synchronized void evaluateRound() {
        if (state != EventState.PLAYING) {
            return;
        }
        Room room = loadEventRoom();
        if (room == null) {
            finishEvent(null, true);
            return;
        }
        pruneMissing(room);

        Set<Integer> seated = new LinkedHashSet<Integer>();
        Set<Integer> usedChairs = new LinkedHashSet<Integer>();

        for (Integer userId : new ArrayList<Integer>(alive)) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);
            if (habbo == null) {
                continue;
            }
            HabboItem chair = seatedChair(habbo);
            if (chair == null || usedChairs.contains(chair.getId())) {
                continue;
            }
            usedChairs.add(chair.getId());
            seated.add(userId);
        }

        List<Integer> eliminated = new ArrayList<Integer>();
        for (Integer userId : new ArrayList<Integer>(alive)) {
            if (!seated.contains(userId)) {
                eliminated.add(userId);
            }
        }

        if (eliminated.isEmpty() && alive.size() > 1) {
            Iterator<Integer> it = alive.iterator();
            Integer last = null;
            while (it.hasNext()) {
                last = it.next();
            }
            if (last != null) {
                eliminated.add(last);
            }
        }

        for (Integer userId : eliminated) {
            alive.remove(userId);
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(userId);
            String name = habbo != null ? habbo.getHabboInfo().getUsername() : ("#" + userId);
            Broadcasts.room(room, SillasLocasConfig.msg("sillaslocas.msg.eliminated").replace("%user%", name));
        }

        if (checkWinOrContinue()) {
            return;
        }

        runLater(seconds(SillasLocasConfig.pauseSeconds()), new Runnable() {
            @Override
            public void run() {
                beginRound();
            }
        });
    }

    private boolean checkWinOrContinue() {
        if (state != EventState.PLAYING) {
            return true;
        }
        if (alive.isEmpty()) {
            finishEvent(null, true);
            return true;
        }
        if (alive.size() == 1) {
            Integer winnerId = alive.iterator().next();
            Habbo winner = Emulator.getGameEnvironment().getHabboManager().getHabbo(winnerId);
            finishEvent(winner, true);
            return true;
        }
        return false;
    }

    private void finishEvent(Habbo winner, boolean scheduleAgain) {
        Room room = loadEventRoom();
        state = EventState.ENDING;

        if (winner != null) {
            String msg = SillasLocasConfig.msg("sillaslocas.msg.winner_room")
                    .replace("%user%", winner.getHabboInfo().getUsername());
            Broadcasts.room(room, msg);
            Broadcasts.hotel(msg);
            Broadcasts.whisper(winner, SillasLocasConfig.msg("sillaslocas.msg.winner_user"));
            PrizeService.give(winner);
        }

        cleanupRoom(room);
        participants.clear();
        alive.clear();
        round = 0;
        state = EventState.IDLE;

        if (scheduleAgain) {
            int minutes = SillasLocasConfig.intervalMinutes();
            Broadcasts.hotel(SillasLocasConfig.msg("sillaslocas.msg.next_in").replace("%minutes%", String.valueOf(minutes)));
            scheduleNextCycle(seconds(minutes * 60));
        }
    }

    private void cleanupRoom(Room room) {
        chairs.clear(room);
        if (room == null || !SillasLocasConfig.kickOnEnd()) {
            return;
        }
        int lobby = SillasLocasConfig.lobbyRoomId();
        List<Habbo> habbos = new ArrayList<Habbo>(room.getHabbos());
        for (Habbo habbo : habbos) {
            if (habbo == null) {
                continue;
            }
            try {
                if (lobby > 0) {
                    teleport(habbo, lobby);
                } else {
                    room.kickHabbo(habbo, false);
                }
            } catch (Exception e) {
                LOGGER.warn("[SillasLocas] No se pudo sacar a {}", habbo.getHabboInfo().getUsername());
            }
        }
    }

    private void syncParticipantsInRoom(Room room) {
        if (room == null) {
            participants.clear();
            return;
        }
        Set<Integer> present = new LinkedHashSet<Integer>();
        for (Habbo habbo : room.getHabbos()) {
            if (habbo != null && participants.contains(habbo.getHabboInfo().getId())) {
                present.add(habbo.getHabboInfo().getId());
            }
        }
        participants.clear();
        participants.addAll(present);
    }

    private void pruneMissing(Room room) {
        List<Integer> gone = new ArrayList<Integer>();
        for (Integer id : alive) {
            Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(id);
            if (habbo == null || habbo.getHabboInfo().getCurrentRoom() == null
                    || habbo.getHabboInfo().getCurrentRoom().getId() != SillasLocasConfig.roomId()) {
                gone.add(id);
            }
        }
        alive.removeAll(gone);
        participants.removeAll(gone);
    }

    private HabboItem seatedChair(Habbo habbo) {
        if (habbo == null || habbo.getRoomUnit() == null) {
            return null;
        }
        if (!isSitting(habbo)) {
            return null;
        }
        int x = habbo.getRoomUnit().getX();
        int y = habbo.getRoomUnit().getY();
        for (HabboItem chair : chairs.items()) {
            if (chair.getX() == x && chair.getY() == y) {
                return chair;
            }
        }
        return null;
    }

    private boolean isSitting(Habbo habbo) {
        try {
            return habbo.getRoomUnit().hasStatus(RoomUnitStatus.SIT);
        } catch (Throwable ignored) {
            return true;
        }
    }

    private Room loadEventRoom() {
        int id = SillasLocasConfig.roomId();
        if (id <= 0) {
            return null;
        }
        return Emulator.getGameEnvironment().getRoomManager().loadRoom(id);
    }

    private void teleport(Habbo habbo, int roomId) {
        try {
            Emulator.getGameEnvironment().getRoomManager().loadRoom(roomId);
            habbo.getClient().sendResponse(new ForwardToRoomComposer(roomId));
        } catch (Exception e) {
            LOGGER.error("[SillasLocas] Teletransporte fallido a sala {}", roomId, e);
        }
    }

    private void scheduleNextCycle(int delayMs) {
        cancelTimers();
        runLater(delayMs, new Runnable() {
            @Override
            public void run() {
                synchronized (SillasLocasManager.this) {
                    beginJoinWindow();
                }
            }
        });
    }

    private synchronized void cancelTimers() {
        timerGeneration++;
    }

    private void runLater(final int delayMs, final Runnable task) {
        final int generation = timerGeneration;
        Emulator.getThreading().run(new Runnable() {
            @Override
            public void run() {
                if (generation != timerGeneration) {
                    return;
                }
                task.run();
            }
        }, delayMs);
    }

    private static int seconds(int value) {
        return Math.max(1, value) * 1000;
    }

    public void shutdown() {
        cancelTimers();
        Room room = loadEventRoom();
        chairs.clear(room);
        state = EventState.IDLE;
        participants.clear();
        alive.clear();
    }
}
