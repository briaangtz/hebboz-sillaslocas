-- Ejecuta esto en la base de datos del hotel (ajusta rank IDs si hace falta).
-- :evento debe estar disponible para todos los rangos.

INSERT IGNORE INTO `emulator_texts` (`key`, `value`) VALUES
('commands.keys.cmd_evento', 'evento'),
('commands.description.cmd_evento', ':evento — Ir a Sillas Locas'),
('commands.keys.cmd_sillaslocas', 'sillaslocas;slocas;sillas'),
('commands.description.cmd_sillaslocas', ':sillaslocas start/stop/reload/status');

-- Permiso de staff para controlar el evento. El comando :evento no usa permiso (todos).
INSERT IGNORE INTO `permissions` (`id`) SELECT `id` FROM `permissions` WHERE 1 LIMIT 0;

-- Si tu tabla permissions es por columnas (Morningstar clásico), añade la columna:
-- ALTER TABLE `permissions` ADD COLUMN `cmd_sillaslocas` ENUM('0','1','2') NOT NULL DEFAULT '0';
-- UPDATE `permissions` SET `cmd_sillaslocas` = '1' WHERE `id` >= 5;
