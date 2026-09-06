package com.sillaslocas;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.GenericAlertComposer;

import java.util.Map;

public final class Broadcasts {

    private Broadcasts() {
    }

    public static void hotel(String message) {
        if (message == null || message.isEmpty()) {
            return;
        }
        GenericAlertComposer composer = new GenericAlertComposer(message);
        for (Map.Entry<Integer, Habbo> entry : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
            Habbo habbo = entry.getValue();
            if (habbo == null || habbo.getClient() == null) {
                continue;
            }
            try {
                if (habbo.getHabboStats() != null && habbo.getHabboStats().blockStaffAlerts) {
                    continue;
                }
            } catch (Exception ignored) {
            }
            habbo.getClient().sendResponse(composer);
        }
    }

    public static void room(Room room, String message) {
        if (room == null || message == null) {
            return;
        }
        room.sendComposer(new GenericAlertComposer(message).compose());
    }

    public static void whisper(Habbo habbo, String message) {
        if (habbo == null || message == null) {
            return;
        }
        habbo.whisper(message, RoomChatMessageBubbles.ALERT);
    }
}
