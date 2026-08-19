package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledPermanentCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/** Handles removing one allowed counter from a permanent the activating player controls. */
public class RemoveCounterFromPermanentCostHandler implements PermanentChoiceCostHandler {

    private final RemoveCounterFromControlledPermanentCost cost;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    public RemoveCounterFromPermanentCostHandler(RemoveCounterFromControlledPermanentCost cost,
                                                 GameLogService gameLogService,
                                                 PredicateEvaluationService predicateEvaluationService) {
        this.cost = cost;
        this.gameLogService = gameLogService;
        this.predicateEvaluationService = predicateEvaluationService;
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
            throw new IllegalStateException("No permanent you control has a counter to remove");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(p -> cost.permanentFilter() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.permanentFilter()))
                .filter(this::hasCounter)
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
        if (battlefield == null || !battlefield.contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent you control");
        }
        if (cost.permanentFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.permanentFilter())) {
            throw new IllegalStateException("Permanent does not match the counter-removal cost");
        }
        CounterType counterType = firstCounterType(chosen);
        if (counterType == null) {
            throw new IllegalStateException("Permanent has no counter to remove");
        }

        chosen.setCounterCount(counterType, chosen.getCounterCount(counterType) - 1);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " removes a counter from ", chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to remove a counter from as an activation cost.";
    }

    private boolean hasCounter(Permanent permanent) {
        return firstCounterType(permanent) != null;
    }

    private CounterType firstCounterType(Permanent permanent) {
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getValue() > 0 && cost.allows(entry.getKey()))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }
}
