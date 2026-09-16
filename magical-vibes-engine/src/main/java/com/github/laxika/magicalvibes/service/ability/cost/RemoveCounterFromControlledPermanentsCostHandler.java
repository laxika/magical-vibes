package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveOneOrMoreCountersFromControlledPermanentsCost;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

import java.util.List;
import java.util.UUID;

/** Handles variable counter costs paid from matching permanents the activating player controls. */
public class RemoveCounterFromControlledPermanentsCostHandler implements PermanentChoiceCostHandler {

    private final RemoveOneOrMoreCountersFromControlledPermanentsCost cost;
    private final int requiredCount;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private int pendingBulkRemoval;
    private int lastRemoved = 1;

    public RemoveCounterFromControlledPermanentsCostHandler(
            RemoveOneOrMoreCountersFromControlledPermanentsCost cost,
            int xValue,
            PredicateEvaluationService predicateEvaluationService,
            GameLogService gameLogService) {
        this.cost = cost;
        this.requiredCount = xValue;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
    }

    @Override
    public CardEffect costEffect() {
        return cost;
    }

    @Override
    public int requiredCount() {
        return requiredCount;
    }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (requiredCount < 1) {
            throw new IllegalStateException("Must remove at least one " + counterLabel() + " counter");
        }
        if (totalCounters(gameData, playerId) < requiredCount) {
            throw new IllegalStateException("Not enough " + counterLabel() + " counters on matching permanents");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }
        return battlefield.stream()
                .filter(p -> matchesFilter(gameData, p))
                .filter(p -> counterCount(p) > 0)
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(player.getId());
        if (battlefield == null || !battlefield.contains(chosen)) {
            throw new IllegalStateException("Must choose a permanent you control");
        }
        if (!matchesFilter(gameData, chosen)) {
            throw new IllegalStateException("Permanent does not match the counter-removal restriction");
        }

        int available = counterCount(chosen);
        int toRemove = pendingBulkRemoval > 0 ? pendingBulkRemoval : 1;
        pendingBulkRemoval = 0;
        if (available < toRemove) {
            throw new IllegalStateException("Not enough " + counterLabel() + " counters on the chosen permanent");
        }

        chosen.setCounterCount(cost.counterType(), available - toRemove);
        lastRemoved = toRemove;
        String counterWord = toRemove == 1
                ? "a " + counterLabel() + " counter"
                : toRemove + " " + counterLabel() + " counters";
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " removes " + counterWord + " from ", chosen.getCard(), " as a cost."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a permanent to remove a " + counterLabel() + " counter from ("
                + remaining + " remaining).";
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
            if (only != null && counterCount(only) >= remaining) {
                pendingBulkRemoval = remaining;
                return true;
            }
        }
        return totalCounters(gameData, playerId) == remaining;
    }

    private boolean matchesFilter(GameData gameData, Permanent permanent) {
        return cost.permanentPredicate() == null
                || predicateEvaluationService.matchesPermanentPredicate(
                gameData, permanent, cost.permanentPredicate());
    }

    private int totalCounters(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return 0;
        }
        return battlefield.stream()
                .filter(p -> matchesFilter(gameData, p))
                .mapToInt(this::counterCount)
                .sum();
    }

    private int counterCount(Permanent permanent) {
        if (cost.counterType() != CounterType.ANY) {
            return permanent.getCounterCount(cost.counterType());
        }
        return permanent.getCounters().entrySet().stream()
                .filter(entry -> entry.getKey() != CounterType.ANY && entry.getKey() != CounterType.SILVER)
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

    private String counterLabel() {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case CHARGE -> "charge";
            default -> cost.counterType().name().toLowerCase().replace('_', ' ');
        };
    }
}
