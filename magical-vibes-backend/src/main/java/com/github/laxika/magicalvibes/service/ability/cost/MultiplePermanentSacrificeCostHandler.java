package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.service.GameQueryService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link SacrificeMultiplePermanentsCost} — the player must sacrifice N permanents
 * matching a {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter
 * (e.g. "Sacrifice three artifacts"). Unlike the single-sacrifice handlers, {@link #requiredCount()}
 * returns the count from the cost record, and the player may be prompted multiple times.
 */
public class MultiplePermanentSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeMultiplePermanentsCost cost;
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;

    /**
     * @param cost            the cost effect record specifying the count and filter predicate
     * @param gameQueryService used to evaluate the permanent predicate
     * @param sacrificeAction  callback that performs the actual sacrifice
     */
    public MultiplePermanentSacrificeCostHandler(SacrificeMultiplePermanentsCost cost, GameQueryService gameQueryService, PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.sacrificeAction = sacrificeAction;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return cost.count(); }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < cost.count()) {
            throw new IllegalStateException("Not enough permanents to sacrifice (need " + cost.count() + ", have " + validIds.size() + ")");
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
            throw new IllegalStateException("Must sacrifice a matching permanent");
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to sacrifice (" + remaining + " remaining).";
    }
}
