package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapXPermanentsCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link TapXPermanentsCost} — the player must tap X untapped permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter,
 * where X is the xValue chosen at activation time (e.g. "Tap X untapped Knights you control").
 */
public class TapXPermanentsCostHandler implements PermanentChoiceCostHandler {

    private final TapXPermanentsCost cost;
    private final int xValue;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;
    private final UUID sourcePermanentId;

    public TapXPermanentsCostHandler(TapXPermanentsCost cost, int xValue,
                                     GameQueryService gameQueryService,
                                     GameBroadcastService gameBroadcastService,
                                     TriggerCollectionService triggerCollectionService,
                                     UUID sourcePermanentId) {
        this.cost = cost;
        this.xValue = xValue;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return xValue; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (xValue <= 0) return;
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < xValue) {
            throw new IllegalStateException("Not enough untapped permanents to tap (need " + xValue + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !p.isTapped())
                .filter(p -> !cost.excludeSource() || !p.getId().equals(sourcePermanentId))
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (chosen.isTapped()) {
            throw new IllegalStateException("Permanent is already tapped");
        }
        if (cost.excludeSource() && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot tap the source permanent for this cost");
        }
        if (!gameQueryService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        chosen.tap();
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
        String tapLog = player.getUsername() + " taps " + chosen.getCard().getName() + " as a cost.";
        gameBroadcastService.logAndBroadcast(gameData, tapLog);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an untapped permanent to tap (" + remaining + " remaining).";
    }
}
