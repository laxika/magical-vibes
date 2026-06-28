package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link ReturnMultiplePermanentsToHandCost} — the player must return N permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter from
 * the battlefield to their owner's hand (e.g. "Return two lands you control to their owner's hand").
 */
public class MultiplePermanentReturnToHandCostHandler implements PermanentChoiceCostHandler {

    private final ReturnMultiplePermanentsToHandCost cost;
    private final GameQueryService gameQueryService;
    private final PermanentBounceAction bounceAction;

    /**
     * @param cost            the cost effect record specifying the count and filter predicate
     * @param gameQueryService used to evaluate the permanent predicate
     * @param bounceAction    callback that performs the actual return-to-hand
     */
    public MultiplePermanentReturnToHandCostHandler(ReturnMultiplePermanentsToHandCost cost,
                                                     GameQueryService gameQueryService,
                                                     PermanentBounceAction bounceAction) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.bounceAction = bounceAction;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return cost.count(); }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < cost.count()) {
            throw new IllegalStateException("Not enough permanents to return to hand (need " + cost.count() + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Must return a matching permanent");
        }
        bounceAction.bounce(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to return to hand (" + remaining + " remaining).";
    }
}
