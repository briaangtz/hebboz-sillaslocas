package com.sillaslocas.listeners;

import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.events.furniture.FurnitureMovedEvent;
import com.eu.habbo.plugin.events.furniture.FurniturePickedUpEvent;
import com.eu.habbo.plugin.events.users.UserExitRoomEvent;
import com.sillaslocas.SillasLocasPlugin;

public class RoomListener implements EventListener {

    @EventHandler
    public void onExit(UserExitRoomEvent event) {
        if (event.habbo != null && event.habbo.getHabboInfo().getCurrentRoom() != null) {
            SillasLocasPlugin.getManager().onUserLeft(event.habbo, event.habbo.getHabboInfo().getCurrentRoom());
        }
    }

    @EventHandler
    public void onMove(FurnitureMovedEvent event) {
        if (event.furniture != null && SillasLocasPlugin.getManager().chairs().isManaged(event.furniture)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickup(FurniturePickedUpEvent event) {
        if (event.furniture != null && SillasLocasPlugin.getManager().chairs().isManaged(event.furniture)) {
            event.setCancelled(true);
        }
    }
}
