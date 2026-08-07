package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link TapMultiplePermanentsCost} — the player must tap N untapped permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter
 * (e.g. "Tap five untapped Myr you control", or "Tap X untapped Knights you control" where X is the
 * value announced at activation). Unlike the single-tap handler, {@link #requiredCount()} returns
 * that count, and the player may be prompted multiple times.
 *
 * <p>The cost's {@link com.github.laxika.magicalvibes.model.amount.DynamicAmount} count is evaluated
 * by the caller and passed in, because {@link #requiredCount()} has no game state to evaluate it
 * against.
 */
public class MultiplePermanentTapCostHandler implements PermanentChoiceCostHandler {

    private final TapMultiplePermanentsCost cost;
    private final int requiredCount;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final UUID sourcePermanentId;

    public MultiplePermanentTapCostHandler(TapMultiplePermanentsCost cost, int requiredCount,
                                           PredicateEvaluationService predicateEvaluationService,
                                           GameLogService gameLogService,
                                           TriggerCollectionService triggerCollectionService,
                                           UUID sourcePermanentId) {
        this.cost = cost;
        this.requiredCount = requiredCount;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return requiredCount; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < requiredCount) {
            throw new IllegalStateException("Not enough untapped permanents to tap (need " + requiredCount + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> !p.isTapped())
                .filter(p -> !cost.excludeSource() || !p.getId().equals(sourcePermanentId))
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
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
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        chosen.tap();
        triggerCollectionService.checkEnchantedPermanentTapTriggers(gameData, chosen);
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " taps " , chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose an untapped permanent to tap (" + remaining + " remaining).";
    }
}
