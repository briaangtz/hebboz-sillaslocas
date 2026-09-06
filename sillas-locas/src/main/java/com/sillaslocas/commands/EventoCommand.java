package com.sillaslocas.commands;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.sillaslocas.SillasLocasPlugin;

public class EventoCommand extends Command {

    public EventoCommand(String permission, String[] keys) {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (gameClient == null || gameClient.getHabbo() == null) {
            return true;
        }
        SillasLocasPlugin.getManager().tryJoin(gameClient.getHabbo());
        return true;
    }
}
