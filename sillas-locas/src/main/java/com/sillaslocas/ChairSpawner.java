package com.sillaslocas;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.RemoveFloorItemComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class ChairSpawner {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChairSpawner.class);
    private final List<HabboItem> spawned = new ArrayList<HabboItem>();

    public synchronized List<HabboItem> spawn(Room room, int chairCount) {
        clear(room);
        if (room == null || chairCount <= 0) {
            return spawned;
        }

        Item base = Emulator.getGameEnvironment().getItemManager().getItem(SillasLocasConfig.chairItemId());
        if (base == null) {
            LOGGER.error("[SillasLocas] sillaslocas.chair_item_id no es un items_base válido.");
            return spawned;
        }

        List<ChairSlot> slots = ChairLayout.resolve(chairCount);
        int ownerId = room.getOwnerId();

        for (ChairSlot slot : slots) {
            try {
                HabboItem item = Emulator.getGameEnvironment().getItemManager().createItem(ownerId, base, 0, 0, "0");
                if (item == null) {
                    continue;
                }
                item.setRoomId(room.getId());
                item.setX(slot.x);
                item.setY(slot.y);
                item.setZ(0.0);
                item.setRotation(slot.rotation);
                item.needsUpdate(true);
                room.addHabboItem(item);
                room.updateItem(item);
                room.sendComposer(new AddFloorItemComposer(item, SillasLocasConfig.ownerName()).compose());
                spawned.add(item);
            } catch (Exception e) {
                LOGGER.error("[SillasLocas] No se pudo colocar una silla en {},{}", slot.x, slot.y, e);
            }
        }
        return spawned;
    }

    public synchronized void clear(Room room) {
        if (spawned.isEmpty()) {
            return;
        }
        List<HabboItem> copy = new ArrayList<HabboItem>(spawned);
        spawned.clear();
        for (HabboItem item : copy) {
            remove(room, item);
        }
    }

    public synchronized boolean isManaged(HabboItem item) {
        if (item == null) {
            return false;
        }
        for (HabboItem spawnedItem : spawned) {
            if (spawnedItem.getId() == item.getId()) {
                return true;
            }
        }
        return false;
    }

    public synchronized List<HabboItem> items() {
        return new ArrayList<HabboItem>(spawned);
    }

    public static void remove(Room room, HabboItem item) {
        if (item == null) {
            return;
        }
        try {
            if (room != null) {
                room.pickUpItem(item, null);
            } else {
                item.setRoomId(0);
                item.needsDelete(true);
                Emulator.getThreading().run(item);
            }
        } catch (Exception primary) {
            try {
                if (room != null) {
                    room.removeHabboItem(item);
                    item.setRoomId(0);
                    item.needsUpdate(true);
                    room.sendComposer(new RemoveFloorItemComposer(item).compose());
                    Emulator.getThreading().run(item);
                }
            } catch (Exception secondary) {
                LOGGER.error("[SillasLocas] Error al retirar silla {}", item.getId(), secondary);
            }
        }
    }
}
