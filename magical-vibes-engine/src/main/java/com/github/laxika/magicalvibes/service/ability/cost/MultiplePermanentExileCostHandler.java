package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExilePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Handles predicate-based permanent exile costs for activated abilities.
 */
public class MultiplePermanentExileCostHandler implements PermanentChoiceCostHandler {

    private final ExilePermanentCost cost;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PermanentExileAction exileAction;
    private final UUID sourcePermanentId;

    public MultiplePermanentExileCostHandler(ExilePermanentCost cost,
                                             PredicateEvaluationService predicateEvaluationService,
                                             PermanentExileAction exileAction,
                                             UUID sourcePermanentId) {
        this.cost = cost;
        this.predicateEvaluationService = predicateEvaluationService;
        this.exileAction = exileAction;
        this.sourcePermanentId = cost.excludeSource() ? sourcePermanentId : null;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public int requiredCount() {
        return 1;
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No permanent to exile matching: " + cost.description());
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.filter()))
                .filter(p -> sourcePermanentId == null || !p.getId().equals(sourcePermanentId))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        PermanentPredicate filter = cost.filter();
        if (!predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, filter)) {
            throw new IllegalStateException("Must exile a permanent matching: " + cost.description());
        }
        if (sourcePermanentId != null && chosen.getId().equals(sourcePermanentId)) {
            throw new IllegalStateException("Cannot exile this permanent to its own ability");
        }
        exileAction.exile(gameData, player, chosen);
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to exile (" + cost.description() + ").";
    }
}
