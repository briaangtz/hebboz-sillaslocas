package com.sillaslocas.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.sillaslocas.SillasLocasConfig;
import com.sillaslocas.SillasLocasPlugin;

public class SillasLocasAdminCommand extends Command {

    public SillasLocasAdminCommand(String permission, String[] keys) {
        super(permission, keys);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (gameClient == null || gameClient.getHabbo() == null) {
            return true;
        }

        String action = params.length > 1 ? params[1].toLowerCase() : "status";

        if ("start".equals(action) || "iniciar".equals(action)) {
            SillasLocasPlugin.getManager().forceStart();
            whisper(gameClient, "Sillas Locas: inscripción iniciada y alerta global enviada.");
            return true;
        }
        if ("stop".equals(action) || "parar".equals(action)) {
            SillasLocasPlugin.getManager().forceStop();
            whisper(gameClient, "Sillas Locas: evento detenido y sala limpiada.");
            return true;
        }
        if ("reload".equals(action) || "recargar".equals(action)) {
            Emulator.getConfig().reload();
            whisper(gameClient, "Sillas Locas: configuración recargada. " + SillasLocasPlugin.getManager().statusLine());
            return true;
        }

        whisper(gameClient, SillasLocasPlugin.getManager().statusLine()
                + " | enabled=" + SillasLocasConfig.enabled()
                + " | room=" + SillasLocasConfig.roomId()
                + " | intervalo=" + SillasLocasConfig.intervalMinutes() + "m"
                + " | premio=" + SillasLocasConfig.prizeType() + " " + SillasLocasConfig.prizeAmount());
        whisper(gameClient, "Uso: :sillaslocas start | stop | reload | status");
        return true;
    }

    private void whisper(GameClient client, String text) {
        client.getHabbo().whisper(text, RoomChatMessageBubbles.ALERT);
    }
}
