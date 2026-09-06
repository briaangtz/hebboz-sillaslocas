package com.sillaslocas;

import java.util.ArrayList;
import java.util.List;

public final class ChairLayout {

    private ChairLayout() {
    }

    public static List<ChairSlot> resolve(int needed) {
        List<ChairSlot> configured = parsePositions(SillasLocasConfig.chairPositions());
        if (configured.size() >= needed) {
            return configured.subList(0, needed);
        }

        List<ChairSlot> slots = new ArrayList<ChairSlot>(configured);
        slots.addAll(gridSlots(needed - slots.size()));
        return slots.size() > needed ? slots.subList(0, needed) : slots;
    }

    private static List<ChairSlot> parsePositions(String raw) {
        List<ChairSlot> slots = new ArrayList<ChairSlot>();
        if (raw == null || raw.trim().isEmpty()) {
            return slots;
        }
        int defaultRot = SillasLocasConfig.chairRotation();
        String[] parts = raw.split(";");
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String[] xy = trimmed.split(",");
            if (xy.length < 2) {
                continue;
            }
            try {
                int x = Integer.parseInt(xy[0].trim());
                int y = Integer.parseInt(xy[1].trim());
                int rot = xy.length >= 3 ? Integer.parseInt(xy[2].trim()) : defaultRot;
                slots.add(new ChairSlot(x, y, rot));
            } catch (NumberFormatException ignored) {
            }
        }
        return slots;
    }

    private static List<ChairSlot> gridSlots(int count) {
        List<ChairSlot> slots = new ArrayList<ChairSlot>();
        int originX = 8;
        int originY = 8;
        String origin = SillasLocasConfig.gridOrigin();
        if (origin != null && origin.contains(",")) {
            String[] p = origin.split(",");
            try {
                originX = Integer.parseInt(p[0].trim());
                originY = Integer.parseInt(p[1].trim());
            } catch (NumberFormatException ignored) {
            }
        }
        int width = SillasLocasConfig.gridWidth();
        int rot = SillasLocasConfig.chairRotation();
        for (int i = 0; i < count; i++) {
            int x = originX + (i % width);
            int y = originY + (i / width);
            slots.add(new ChairSlot(x, y, rot));
        }
        return slots;
    }
}
