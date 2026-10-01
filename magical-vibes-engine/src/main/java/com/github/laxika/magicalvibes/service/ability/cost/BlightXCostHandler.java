package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.BlightXCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;

import java.util.List;
import java.util.UUID;

/** Pays an activated-ability {@link BlightXCost} by putting X -1/-1 counters on a chosen creature. */
public class BlightXCostHandler implements PermanentChoiceCostHandler {

    private final BlightXCost cost;
    private final int xValue;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    public BlightXCostHandler(BlightXCost cost, int xValue, GameQueryService gameQueryService,
                              AmountEvaluationService amountEvaluationService,
                              GameLogService gameLogService) {
        this.cost = cost;
        this.xValue = xValue;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.gameLogService = gameLogService;
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
        if (xValue < 0) {
            throw new IllegalStateException("X can't be negative");
        }
        if (cost.maximumX() != null) {
            int maximumX = amountEvaluationService.evaluate(
                    gameData, cost.maximumX(), new AmountContext(playerId, null, null, xValue, 0));
            if (xValue > maximumX) {
                throw new IllegalStateException("X can't be greater than " + maximumX);
            }
        }
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No creature you control to blight");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must choose a creature to blight");
        }
        int count = gameQueryService.replaceCounters(
                gameData, chosen, CounterType.MINUS_ONE_MINUS_ONE, xValue, player.getId());
        if (count <= 0) {
            return;
        }
        chosen.setCounterCount(
                CounterType.MINUS_ONE_MINUS_ONE,
                chosen.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);
        gameData.playersWhoPutCountersOnCreaturesThisTurn.add(player.getId());
        String counterWord = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " puts " + counterWord + " on ", chosen.getCard(), "."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a creature to blight " + xValue + ".";
    }
}
