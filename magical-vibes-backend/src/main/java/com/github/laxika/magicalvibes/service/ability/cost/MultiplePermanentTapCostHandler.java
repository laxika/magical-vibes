package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link TapMultiplePermanentsCost} — the player must tap N untapped permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter
 * (e.g. "Tap five untapped Myr you control"). Unlike the single-tap handler, {@link #requiredCount()}
 * returns the count from the cost record, and the player may be prompted multiple times.
 */
public class MultiplePermanentTapCostHandler implements PermanentChoiceCostHandler {

    private final TapMultiplePermanentsCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final TriggerCollectionService triggerCollectionService;

    public MultiplePermanentTapCostHandler(TapMultiplePermanentsCost cost, GameQueryService gameQueryService,
                                           GameBroadcastService gameBroadcastService,
                                           TriggerCollectionService triggerCollectionService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
        this.triggerCollectionService = triggerCollectionService;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return cost.count(); }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < cost.count()) {
            throw new IllegalStateException("Not enough untapped permanents to tap (need " + cost.count() + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !p.isTapped())
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (chosen.isTapped()) {
            throw new IllegalStateException("Permanent is already tapped");
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
