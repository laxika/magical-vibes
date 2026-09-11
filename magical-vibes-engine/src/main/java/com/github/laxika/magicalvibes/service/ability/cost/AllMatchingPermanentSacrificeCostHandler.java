package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAllMatchingPermanentsCost;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameQueryService gameQueryService;
    private final PermanentSacrificeAction sacrificeAction;

    public AllMatchingPermanentSacrificeCostHandler(SacrificeAllMatchingPermanentsCost cost,
                                                      PredicateEvaluationService predicateEvaluationService,
                                                      PermanentSacrificeAction sacrificeAction) {
        this(cost, predicateEvaluationService, null, sacrificeAction);
    }

    public AllMatchingPermanentSacrificeCostHandler(SacrificeAllMatchingPermanentsCost cost,
                                                      PredicateEvaluationService predicateEvaluationService,
                                                      GameQueryService gameQueryService,
                                                      PermanentSacrificeAction sacrificeAction) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameQueryService = gameQueryService;
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
        if (gameQueryService != null && matchingPermanents(gameData, playerId).stream()
                .anyMatch(permanent -> !gameQueryService.canSacrificePermanentForCosts(gameData, permanent))) {
            throw new IllegalStateException("A matching permanent cannot be sacrificed as a cost");
        }
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
                .filter(permanent -> gameQueryService == null
                        || gameQueryService.canSacrificePermanentForCosts(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.filter())) {
            throw new IllegalStateException("Permanent does not match the required predicate");
        }
        if (gameQueryService != null && !gameQueryService.canSacrificePermanentForCosts(gameData, chosen)) {
            throw new IllegalStateException("This permanent cannot be sacrificed as a cost");
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

    private List<Permanent> matchingPermanents(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                        gameData, permanent, cost.filter()))
                .toList();
    }
}
