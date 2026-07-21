package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromControlledCreatureCost;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Handles the {@link RemoveCounterFromControlledCreatureCost} — counters may come from among
 * creatures you control (split across creatures or all from one). Each interactive pick removes
 * one counter; when only one legal distribution remains, payment auto-completes (bulk-removing
 * from a single creature when needed).
 */
public class RemoveCounterFromCreatureCostHandler implements PermanentChoiceCostHandler {

    private final RemoveCounterFromControlledCreatureCost cost;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private int pendingBulkRemoval;
    private int lastRemoved = 1;

    public RemoveCounterFromCreatureCostHandler(RemoveCounterFromControlledCreatureCost cost,
                                                 GameQueryService gameQueryService,
                                                 GameBroadcastService gameBroadcastService) {
        this.cost = cost;
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
    }

    @Override public CardEffect costEffect() { return cost; }

    @Override public int requiredCount() { return cost.count(); }

    @Override public int lastPaymentWeight() { return lastRemoved; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (totalCounters(gameData, playerId) < cost.count()) {
            throw new IllegalStateException("No creature with enough " + counterLabel() + " counters to remove");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> getCounterCount(p) >= 1)
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must choose a creature");
        }
        int toRemove = pendingBulkRemoval > 0 ? pendingBulkRemoval : 1;
        pendingBulkRemoval = 0;
        int available = getCounterCount(chosen);
        if (available < toRemove) {
            throw new IllegalStateException("Not enough " + counterLabel() + " counters on " + chosen.getCard().getName());
        }

        removeCounters(chosen, toRemove);
        lastRemoved = toRemove;

        String counterWord = toRemove == 1
                ? "a " + counterLabel() + " counter"
                : toRemove + " " + counterLabel() + " counters";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(player.getUsername() + " removes " + counterWord + " from " , chosen.getCard(), "."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        if (remaining > 1) {
            return "Choose a creature to remove a " + counterLabel() + " counter from (" + remaining + " remaining).";
        }
        return "Choose a creature to remove a " + counterLabel() + " counter from.";
    }

    @Override
    public boolean canPayRemaining(GameData gameData, UUID playerId, int remaining) {
        return totalCounters(gameData, playerId) >= remaining;
    }

    @Override
    public boolean shouldAutoPayAll(GameData gameData, UUID playerId, int remaining) {
        List<UUID> validIds = getValidChoiceIds(gameData, playerId);
        if (validIds.isEmpty()) {
            return true;
        }
        // One creature holds all remaining counters — remove them in a single payment.
        if (validIds.size() == 1) {
            Permanent only = gameQueryService.findPermanentById(gameData, validIds.getFirst());
            if (only != null && getCounterCount(only) >= remaining) {
                pendingBulkRemoval = remaining;
                return true;
            }
            return false;
        }
        // Exactly `remaining` creatures each with one counter — only one legal distribution.
        if (validIds.size() == remaining
                && validIds.stream()
                .map(id -> gameQueryService.findPermanentById(gameData, id))
                .filter(Objects::nonNull)
                .allMatch(p -> getCounterCount(p) == 1)) {
            return true;
        }
        return false;
    }

    private int totalCounters(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .mapToInt(this::getCounterCount)
                .sum();
    }

    private int getCounterCount(Permanent permanent) {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            case MINUS_ONE_MINUS_ONE -> permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            case CHARGE -> permanent.getCounterCount(CounterType.CHARGE);
            case ANY -> permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            default -> 0;
        };
    }

    private void removeCounters(Permanent permanent, int count) {
        switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - count);
            case MINUS_ONE_MINUS_ONE -> permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) - count);
            case CHARGE -> permanent.setCounterCount(CounterType.CHARGE, permanent.getCounterCount(CounterType.CHARGE) - count);
            case ANY -> {
                int remaining = count;
                int minus = permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
                int removeMinus = Math.min(minus, remaining);
                if (removeMinus > 0) {
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, minus - removeMinus);
                    remaining -= removeMinus;
                }
                if (remaining > 0) {
                    permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                            permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - remaining);
                }
            }
            default -> { }
        }
    }

    private String counterLabel() {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case CHARGE -> "charge";
            default -> "";
        };
    }
}
