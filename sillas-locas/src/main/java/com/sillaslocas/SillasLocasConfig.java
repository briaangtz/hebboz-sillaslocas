package com.sillaslocas;

import com.eu.habbo.Emulator;

/**
 * All tunables live in emulator_settings via Emulator.getConfig().
 * Change values in the database (or config.ini) and run :sillaslocas reload.
 */
public final class SillasLocasConfig {

    private SillasLocasConfig() {
    }

    public static void registerDefaults() {
        register("sillaslocas.enabled", "1");
        register("sillaslocas.room_id", "0");
        register("sillaslocas.lobby_room_id", "0");
        register("sillaslocas.interval_minutes", "15");
        register("sillaslocas.startup_delay_seconds", "30");
        register("sillaslocas.warn_seconds", "45");
        register("sillaslocas.join_seconds", "75");
        register("sillaslocas.min_players", "2");
        register("sillaslocas.max_players", "24");
        register("sillaslocas.round_walk_seconds", "12");
        register("sillaslocas.round_sit_seconds", "8");
        register("sillaslocas.round_pause_seconds", "5");
        register("sillaslocas.kick_on_end", "1");
        register("sillaslocas.chair_item_id", "0");
        register("sillaslocas.chair_rotation", "2");
        register("sillaslocas.chair_positions", "");
        register("sillaslocas.grid_origin", "8,8");
        register("sillaslocas.grid_width", "8");
        register("sillaslocas.prize.type", "CREDITS");
        register("sillaslocas.prize.amount", "100");
        register("sillaslocas.prize.currency_type", "5");
        register("sillaslocas.prize.badge", "");
        register("sillaslocas.prize.item_id", "0");
        register("sillaslocas.owner_name", "Hotel");

        register("sillaslocas.msg.alert",
                "📢 ¡SILLAS LOCAS!\n🪑 El evento comenzará en breve.\n🚪 Usa :evento para ingresar a la sala y participar.\n🎁 ¡Hay premios para los ganadores!");
        register("sillaslocas.msg.teleport_ok",
                "🪑 Has sido enviado al evento de Sillas Locas. ¡Buena suerte!");
        register("sillaslocas.msg.not_active",
                "🪑 El evento de Sillas Locas todavía no comenzó o ya terminó. ¡Espera la próxima alerta!");
        register("sillaslocas.msg.disabled",
                "🪑 El evento automático de Sillas Locas está desactivado.");
        register("sillaslocas.msg.full",
                "🪑 El evento está lleno. Inténtalo en la próxima ronda.");
        register("sillaslocas.msg.room_missing",
                "🪑 La sala del evento no está configurada. Avisa a un administrador.");
        register("sillaslocas.msg.already_playing",
                "🪑 El evento ya está en curso. Espera a que termine para unirte.");
        register("sillaslocas.msg.cancelled_few",
                "🪑 Sillas Locas cancelado: no hay suficientes jugadores. Próximo intento en el intervalo configurado.");
        register("sillaslocas.msg.music_on",
                "🎵 ¡La música suena! Camina alrededor de las sillas...");
        register("sillaslocas.msg.music_off",
                "🛑 ¡La música se detuvo! ¡Siéntate en una silla!");
        register("sillaslocas.msg.eliminated",
                "❌ %user% se ha quedado sin silla y ha sido eliminado.");
        register("sillaslocas.msg.round",
                "🪑 Ronda %round% — sillas: %chairs% | jugadores: %players%");
        register("sillaslocas.msg.winner_room",
                "🏆 ¡Tenemos ganador!\n🎉 ¡Felicidades, @%user%!\n🎁 Has ganado el premio de Sillas Locas.");
        register("sillaslocas.msg.winner_user",
                "🎁 Has ganado el premio de Sillas Locas.");
        register("sillaslocas.msg.next_in",
                "⏳ Próximo evento de Sillas Locas en %minutes% minutos.");
        register("sillaslocas.msg.starting",
                "🏁 ¡Sillas Locas comienza ahora! Preparaos...");
    }

    private static void register(String key, String value) {
        Emulator.getConfig().register(key, value);
        Emulator.getTexts().register(key, value);
    }

    public static boolean enabled() {
        return Emulator.getConfig().getBoolean("sillaslocas.enabled", true);
    }

    public static int roomId() {
        return Emulator.getConfig().getInt("sillaslocas.room_id", 0);
    }

    public static int lobbyRoomId() {
        return Emulator.getConfig().getInt("sillaslocas.lobby_room_id", 0);
    }

    public static int intervalMinutes() {
        return Math.max(1, Emulator.getConfig().getInt("sillaslocas.interval_minutes", 15));
    }

    public static int startupDelaySeconds() {
        return Math.max(5, Emulator.getConfig().getInt("sillaslocas.startup_delay_seconds", 30));
    }

    public static int warnSeconds() {
        return Math.max(5, Emulator.getConfig().getInt("sillaslocas.warn_seconds", 45));
    }

    public static int joinSeconds() {
        return Math.max(10, Emulator.getConfig().getInt("sillaslocas.join_seconds", 75));
    }

    public static int minPlayers() {
        return Math.max(2, Emulator.getConfig().getInt("sillaslocas.min_players", 2));
    }

    public static int maxPlayers() {
        return Math.max(minPlayers(), Emulator.getConfig().getInt("sillaslocas.max_players", 24));
    }

    public static int walkSeconds() {
        return Math.max(3, Emulator.getConfig().getInt("sillaslocas.round_walk_seconds", 12));
    }

    public static int sitSeconds() {
        return Math.max(2, Emulator.getConfig().getInt("sillaslocas.round_sit_seconds", 8));
    }

    public static int pauseSeconds() {
        return Math.max(1, Emulator.getConfig().getInt("sillaslocas.round_pause_seconds", 5));
    }

    public static boolean kickOnEnd() {
        return Emulator.getConfig().getBoolean("sillaslocas.kick_on_end", true);
    }

    public static int chairItemId() {
        return Emulator.getConfig().getInt("sillaslocas.chair_item_id", 0);
    }

    public static int chairRotation() {
        return Emulator.getConfig().getInt("sillaslocas.chair_rotation", 2);
    }

    public static String chairPositions() {
        return Emulator.getConfig().getValue("sillaslocas.chair_positions", "");
    }

    public static String gridOrigin() {
        return Emulator.getConfig().getValue("sillaslocas.grid_origin", "8,8");
    }

    public static int gridWidth() {
        return Math.max(1, Emulator.getConfig().getInt("sillaslocas.grid_width", 8));
    }

    public static String prizeType() {
        return Emulator.getConfig().getValue("sillaslocas.prize.type", "CREDITS").toUpperCase();
    }

    public static int prizeAmount() {
        return Math.max(0, Emulator.getConfig().getInt("sillaslocas.prize.amount", 100));
    }

    public static int prizeCurrencyType() {
        return Emulator.getConfig().getInt("sillaslocas.prize.currency_type", 5);
    }

    public static String prizeBadge() {
        return Emulator.getConfig().getValue("sillaslocas.prize.badge", "");
    }

    public static int prizeItemId() {
        return Emulator.getConfig().getInt("sillaslocas.prize.item_id", 0);
    }

    public static String ownerName() {
        return Emulator.getConfig().getValue("sillaslocas.owner_name", "Hotel");
    }

    public static String msg(String key) {
        return Emulator.getConfig().getValue(key, Emulator.getTexts().getValue(key, key));
    }
}
