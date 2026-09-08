package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllMatchingPermanentsCost;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Pays {@link SacrificeAllMatchingPermanentsCost} without prompting: every matching permanent is
 * part of the cost, so the player has no choice about the selection.
 */
public class AllMatchingPermanentSacrificeCostHandler implements PermanentChoiceCostHandler {

    private final SacrificeAllMatchingPermanentsCost cost;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentSacrificeAction sacrificeAction;

    public AllMatchingPermanentSacrificeCostHandler(SacrificeAllMatchingPermanentsCost cost,
                                                      PredicateEvaluationService predicateEvaluationService,
                                                      PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.sacrificeAction = sacrificeAction;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    /** A positive sentinel causes the common payment flow to invoke the automatic payment path. */
    @Override
    public int requiredCount() {
        return 1;
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        // Sacrificing zero matching permanents is legal.
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, cost.filter()))
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
        return "All matching permanents are sacrificed automatically.";
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        return true;
    }
}
