package com.sillaslocas;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.commands.CommandHandler;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.plugin.EventHandler;
import com.eu.habbo.plugin.EventListener;
import com.eu.habbo.plugin.HabboPlugin;
import com.eu.habbo.plugin.events.emulator.EmulatorLoadedEvent;
import com.sillaslocas.commands.EventoCommand;
import com.sillaslocas.commands.SillasLocasAdminCommand;
import com.sillaslocas.listeners.RoomListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SillasLocasPlugin extends HabboPlugin implements EventListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SillasLocasPlugin.class);
    private static SillasLocasManager manager;

    @Override
    public void onEnable() {
        Emulator.getPluginManager().registerEvents(this, this);
        Emulator.getPluginManager().registerEvents(this, new RoomListener());
        LOGGER.info("[SillasLocas] Plugin cargado. Esperando EmulatorLoaded...");
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.shutdown();
        }
        LOGGER.info("[SillasLocas] Plugin detenido.");
    }

    @Override
    public boolean hasPermission(Habbo habbo, String s) {
        return false;
    }

    @EventHandler
    public void onLoaded(EmulatorLoadedEvent event) {
        SillasLocasConfig.registerDefaults();
        manager = new SillasLocasManager();

        Emulator.getTexts().register("commands.keys.cmd_evento", "evento");
        Emulator.getTexts().register("commands.description.cmd_evento", ":evento — Ir a Sillas Locas");
        Emulator.getTexts().register("commands.keys.cmd_sillaslocas", "sillaslocas;slocas;sillas");
        Emulator.getTexts().register("commands.description.cmd_sillaslocas", ":sillaslocas start/stop/reload/status");

        CommandHandler.addCommand(new EventoCommand(null,
                Emulator.getTexts().getValue("commands.keys.cmd_evento").split(";")));
        CommandHandler.addCommand(new SillasLocasAdminCommand("cmd_sillaslocas",
                Emulator.getTexts().getValue("commands.keys.cmd_sillaslocas").split(";")));

        manager.startScheduler();
        LOGGER.info("[SillasLocas] Sistema automático listo. Sala {} | cada {} min.",
                SillasLocasConfig.roomId(), SillasLocasConfig.intervalMinutes());
    }

    public static SillasLocasManager getManager() {
        return manager;
    }
}
