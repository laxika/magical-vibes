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

/** Handles removing allowed counters from permanents the activating player controls. */
public class RemoveCounterFromPermanentCostHandler implements PermanentChoiceCostHandler {

    private final RemoveCounterFromControlledPermanentCost cost;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final UUID sourcePermanentId;
    private int pendingBulkRemoval;
    private boolean autoPayAll;
    private int lastRemoved = 1;

    public RemoveCounterFromPermanentCostHandler(RemoveCounterFromControlledPermanentCost cost,
                                                 GameLogService gameLogService) {
        this(cost, gameLogService, null, null);
    }

    public RemoveCounterFromPermanentCostHandler(RemoveCounterFromControlledPermanentCost cost,
                                                 GameLogService gameLogService,
                                                 PredicateEvaluationService predicateEvaluationService) {
        this(cost, gameLogService, predicateEvaluationService, null);
    }

    public RemoveCounterFromPermanentCostHandler(RemoveCounterFromControlledPermanentCost cost,
                                                 GameLogService gameLogService,
                                                 PredicateEvaluationService predicateEvaluationService,
                                                 UUID sourcePermanentId) {
        this.cost = cost;
        this.gameLogService = gameLogService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.sourcePermanentId = sourcePermanentId;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public int requiredCount() {
        return cost.count();
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        int available = totalCounters(gameData, playerId);
        if (available < cost.count()) {
            throw new IllegalStateException("Not enough counters to remove (need "
                    + cost.count() + ", have " + available + ")");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(p -> !isExcludedSource(p))
                .filter(p -> cost.permanentPredicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.permanentPredicate()))
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
        if (isExcludedSource(chosen)) {
            throw new IllegalStateException("Cannot remove counters from this permanent");
        }
        if (cost.permanentPredicate() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, chosen, cost.permanentPredicate())) {
            throw new IllegalStateException("Permanent does not match the counter-removal restriction");
        }
        int available = totalCounters(chosen);
        if (available == 0) {
            throw new IllegalStateException("Permanent has no counter to remove");
        }

        int toRemove;
        if (pendingBulkRemoval > 0) {
            toRemove = pendingBulkRemoval;
            pendingBulkRemoval = 0;
        } else if (autoPayAll) {
            toRemove = available;
        } else {
            toRemove = 1;
        }
        if (available < toRemove) {
            throw new IllegalStateException("Not enough counters on the chosen permanent");
        }

        removeCounters(gameData, chosen, toRemove);
        lastRemoved = toRemove;
        String counterWord = toRemove == 1 ? "a counter" : toRemove + " counters";
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " removes " + counterWord + " from ", chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to remove a counter from (" + remaining + " remaining).";
    }

    @Override
    public int lastPaymentWeight() {
        return lastRemoved;
    }

    @Override
    public boolean canPayRemaining(GameData gameData, UUID playerId, int remaining) {
        return totalCounters(gameData, playerId) >= remaining;
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.size() == 1) {
            Permanent only = findPermanent(gameData, validIds.getFirst());
            if (only != null && totalCounters(only) >= remaining) {
                pendingBulkRemoval = remaining;
                return true;
            }
        }
        if (totalCounters(gameData, playerId) == remaining) {
            autoPayAll = true;
            return true;
        }
        return false;
    }

    private boolean hasCounter(Permanent permanent) {
        return firstCounterType(permanent) != null;
    }

    private int totalCounters(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        return battlefield.stream()
                .filter(p -> !isExcludedSource(p))
                .filter(p -> cost.permanentPredicate() == null
                        || predicateEvaluationService.matchesPermanentPredicate(gameData, p, cost.permanentPredicate()))
                .mapToInt(this::totalCounters)
                .sum();
    }

    private int totalCounters(Permanent permanent) {
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getValue() > 0
                        && entry.getKey() != CounterType.ANY
                        && entry.getKey() != CounterType.SILVER
                        && cost.allows(entry.getKey()))
                .mapToInt(java.util.Map.Entry::getValue)
                .sum();
    }

    private Permanent findPermanent(GameData gameData, UUID id) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    private CounterType firstCounterType(Permanent permanent) {
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getValue() > 0
                        && entry.getKey() != CounterType.ANY
                        && entry.getKey() != CounterType.SILVER
                        && cost.allows(entry.getKey()))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private boolean isExcludedSource(Permanent permanent) {
        return cost.excludeSource() && sourcePermanentId != null
                && sourcePermanentId.equals(permanent.getId());
    }

    private void removeCounters(GameData gameData, Permanent permanent, int count) {
        int remaining = count;
        for (CounterType counterType : CounterType.values()) {
            if (remaining == 0 || counterType == CounterType.ANY || counterType == CounterType.SILVER
                    || !cost.allows(counterType)) {
                continue;
            }
            int available = permanent.getCounterCount(counterType);
            int removed = Math.min(available, remaining);
            if (removed > 0) {
                permanent.setCounterCount(counterType, available - removed);
                if (counterType == CounterType.OIL) {
                    gameData.recordOilCounterRemoved(permanent, removed);
                }
                remaining -= removed;
            }
        }
    }
}
