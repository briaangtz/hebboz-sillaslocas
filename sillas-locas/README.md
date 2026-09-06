# Sillas Locas automático — Arcturus Morningstar

Plugin para Habbo Holo / Arcturus que programa **Sillas Locas cada 15 minutos**, alerta global, comando `:evento`, sillas automáticas, eliminaciones, premio y limpieza de sala.

Compatible con **Arcturus Morningstar 3.x**. Copia `Habbo.jar` de tu emulador a `lib/Habbo.jar` para compilar.

## Instalación

1. Crea la carpeta `lib` y copia tu `Habbo.jar` (el del emulador) ahí.
2. Compila:

```bash
mvn -q package
```

3. Copia `target/SillasLocas-1.0.0.jar` a la carpeta `plugins` del emulador.
4. Ejecuta `sql/permissions.sql` (textos). Si tu tabla `permissions` usa columnas, añade `cmd_sillaslocas` a los rangos de staff.
5. Reinicia el emulador.

## Configuración (emulator_settings / config.ini)

Tras el primer arranque aparecen las claves. Las más importantes:

| Clave | Qué hace | Ejemplo |
| --- | --- | --- |
| `sillaslocas.enabled` | Activa el ciclo automático | `1` |
| `sillaslocas.room_id` | ID de la sala del evento | `15` |
| `sillaslocas.lobby_room_id` | Sala a la que se envía al terminar (`0` = kick) | `1` |
| `sillaslocas.interval_minutes` | Minutos entre eventos | `15` |
| `sillaslocas.warn_seconds` | Aviso previo | `45` |
| `sillaslocas.join_seconds` | Tiempo extra para entrar con `:evento` | `75` |
| `sillaslocas.min_players` / `max_players` | Cupo | `2` / `24` |
| `sillaslocas.round_walk_seconds` | Tiempo de “música” | `12` |
| `sillaslocas.round_sit_seconds` | Tiempo para sentarse | `8` |
| `sillaslocas.round_pause_seconds` | Pausa entre rondas | `5` |
| `sillaslocas.chair_item_id` | ID de `items_base` de la silla | *(obligatorio)* |
| `sillaslocas.chair_positions` | Casillas `x,y,rot` separadas por `;` | `10,10,2;11,10,2;12,10,2` |
| `sillaslocas.grid_origin` | Si faltan posiciones, genera una cuadrícula | `8,8` |
| `sillaslocas.grid_width` | Ancho de esa cuadrícula | `8` |
| `sillaslocas.prize.type` | `CREDITS`, `PIXELS`, `DIAMONDS`, `CURRENCY`, `BADGE`, `ITEM` | `CREDITS` |
| `sillaslocas.prize.amount` | Cantidad | `100` |
| `sillaslocas.prize.currency_type` | Tipo de moneda si usas `CURRENCY` | `5` |
| `sillaslocas.prize.badge` | Código de placa (opcional extra) | `SILLAS` |
| `sillaslocas.prize.item_id` | `items_base` si el premio es furni | `0` |
| `sillaslocas.kick_on_end` | Sacar jugadores al terminar | `1` |

Los mensajes (`sillaslocas.msg.*`) también son editables sin tocar código.

Cómo sacar el **item_id de la silla**: en la base, tabla `items_base`, columna `id` del furni que se pueda sentar.

Cómo sacar el **room_id**: entra a la sala y mira el ID en el cliente, o en `rooms.id`.

## Comandos

| Comando | Quién | Efecto |
| --- | --- | --- |
| `:evento` | Todos | Teletransporta a la sala si el evento está en inscripción o ya estás jugando |
| `:sillaslocas start` | Staff (`cmd_sillaslocas`) | Fuerza alerta + inscripción ahora |
| `:sillaslocas stop` | Staff | Cancela, limpia sillas y reprograma |
| `:sillaslocas reload` | Staff | Recarga config |
| `:sillaslocas status` | Staff | Estado actual |

## Flujo

Cada N minutos: alerta global → jugadores usan `:evento` → al cerrar inscripción se generan `jugadores - 1` sillas → música (espera) → “¡siéntate!” → quien no esté sentado en una silla del evento queda fuera → se reducen sillas → se repite hasta 1 ganador → premio → limpieza → espera el intervalo.

## Staff

En Morningstar clásico:

```sql
ALTER TABLE `permissions` ADD COLUMN `cmd_sillaslocas` ENUM('0','1','2') NOT NULL DEFAULT '0';
UPDATE `permissions` SET `cmd_sillaslocas` = '1' WHERE `id` >= 5;
```

Luego `:update_permissions`.

Si tu fork usa otra API (`givePoints`, `pickUpItem`, `AddFloorItemComposer`), avisa la versión exacta del emulador y se ajusta el plugin.
