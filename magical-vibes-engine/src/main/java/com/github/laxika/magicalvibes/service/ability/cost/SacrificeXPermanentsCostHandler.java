package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link SacrificeXPermanentsCost} — the player must sacrifice X permanents matching a
 * {@link com.github.laxika.magicalvibes.model.filter.PermanentPredicate} filter, where X is the
 * xValue chosen at activation time (e.g. "Sacrifice X Goats" on Springjack Pasture). The
 * sacrifice-analog of {@link TapXPermanentsCostHandler}.
 */
public class SacrificeXPermanentsCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeXPermanentsCost cost;
    private final int xValue;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentSacrificeAction sacrificeAction;

    public SacrificeXPermanentsCostHandler(SacrificeXPermanentsCost cost, int xValue,
                                           PredicateEvaluationService predicateEvaluationService,
                                           PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.xValue = xValue;
        this.predicateEvaluationService = predicateEvaluationService;
        this.sacrificeAction = sacrificeAction;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return xValue; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (xValue <= 0) return;
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() < xValue) {
            throw new IllegalStateException("Not enough permanents to sacrifice (need " + xValue + ", have " + validIds.size() + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        sacrificeAction.sacrifice(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to sacrifice (" + remaining + " remaining).";
    }
}
