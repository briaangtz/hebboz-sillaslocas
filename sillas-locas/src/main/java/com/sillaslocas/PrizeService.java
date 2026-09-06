package com.sillaslocas;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.inventory.AddHabboItemComposer;
import com.eu.habbo.messages.outgoing.inventory.InventoryRefreshComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class PrizeService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrizeService.class);

    private PrizeService() {
    }

    public static void give(Habbo winner) {
        if (winner == null) {
            return;
        }

        String type = SillasLocasConfig.prizeType();
        try {
            if ("CREDITS".equals(type)) {
                winner.giveCredits(SillasLocasConfig.prizeAmount());
            } else if ("PIXELS".equals(type) || "DUCKETS".equals(type)) {
                winner.givePixels(SillasLocasConfig.prizeAmount());
            } else if ("DIAMONDS".equals(type) || "POINTS".equals(type)) {
                winner.givePoints(SillasLocasConfig.prizeAmount());
            } else if ("CURRENCY".equals(type)) {
                winner.givePoints(SillasLocasConfig.prizeCurrencyType(), SillasLocasConfig.prizeAmount());
            } else if ("BADGE".equals(type)) {
                String badge = SillasLocasConfig.prizeBadge();
                if (badge != null && !badge.isEmpty()) {
                    winner.addBadge(badge);
                }
            } else if ("ITEM".equals(type)) {
                giveItem(winner, SillasLocasConfig.prizeItemId());
            } else {
                winner.giveCredits(SillasLocasConfig.prizeAmount());
            }

            String badgeExtra = SillasLocasConfig.prizeBadge();
            if (!"BADGE".equals(type) && badgeExtra != null && !badgeExtra.isEmpty()) {
                winner.addBadge(badgeExtra);
            }
        } catch (Exception e) {
            LOGGER.error("[SillasLocas] Error entregando premio a {}", winner.getHabboInfo().getUsername(), e);
        }
    }

    private static void giveItem(Habbo winner, int itemId) {
        if (itemId <= 0) {
            return;
        }
        Item base = Emulator.getGameEnvironment().getItemManager().getItem(itemId);
        if (base == null) {
            LOGGER.error("[SillasLocas] prize.item_id {} no existe en items_base", itemId);
            return;
        }
        HabboItem reward = Emulator.getGameEnvironment().getItemManager().createItem(
                winner.getHabboInfo().getId(), base, 0, 0, "");
        if (reward == null) {
            return;
        }
        winner.getInventory().getItemsComponent().addItem(reward);
        winner.getClient().sendResponse(new AddHabboItemComposer(reward));
        winner.getClient().sendResponse(new InventoryRefreshComposer());
    }
}
