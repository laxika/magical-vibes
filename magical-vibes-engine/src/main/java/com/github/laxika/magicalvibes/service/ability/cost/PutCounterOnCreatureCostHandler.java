package com.github.laxika.magicalvibes.service.ability.cost;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnControlledCreatureCost;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;

import java.util.List;
import java.util.UUID;

/**
 * Handles the {@link PutCounterOnControlledCreatureCost} as an activated-ability cost (e.g. Hatchet
 * Bully — "Put a -1/-1 counter on a creature you control", Gristle Glutton's "Blight 1", Blighted Nightmare's
 * "Blight X"). The player chooses a creature they control that can get the counters (CR 701.68b, 118.3) and the
 * counter(s) are placed on it; the creature dies later via state-based actions if its toughness reaches 0. An
 * {@code XValue} amount is the activation's announced X.
 */
public class PutCounterOnCreatureCostHandler implements PermanentChoiceCostHandler {

    private final PutCounterOnControlledCreatureCost cost;
    private final int xValue;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    public PutCounterOnCreatureCostHandler(PutCounterOnControlledCreatureCost cost, int xValue,
                                           GameQueryService gameQueryService,
                                           AmountEvaluationService amountEvaluationService,
                                           GameLogService gameLogService) {
        this.cost = cost;
        this.xValue = xValue;
        this.gameQueryService = gameQueryService;
        this.amountEvaluationService = amountEvaluationService;
        this.gameLogService = gameLogService;
    }

    @Override public CardEffect costEffect() { return cost; }
    @Override public int requiredCount() { return 1; }

    @Override
    public void validateCanPay(GameData gameData, UUID playerId) {
        if (xValue < 0) {
            throw new IllegalStateException("X can't be negative");
        }
        if (getValidChoiceIds(gameData, playerId).isEmpty()) {
            throw new IllegalStateException("No creature you control to put a " + counterLabel() + " counter on");
        }
    }

    @Override
    public List<UUID> getValidChoiceIds(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        boolean placesCounters = count(gameData, playerId) > 0;
        return battlefield.stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .filter(p -> !placesCounters || canReceiveCounters(gameData, p))
                .filter(p -> !gameQueryService.cantBeChosen(gameData, p))
                .map(Permanent::getId)
                .toList();
    }

    @Override
    public void validateAndPay(GameData gameData, Player player, Permanent chosen) {
        if (!gameQueryService.isCreature(gameData, chosen)) {
            throw new IllegalStateException("Must choose a creature");
        }
        int count = count(gameData, player.getId());
        if (count > 0 && !canReceiveCounters(gameData, chosen)) {
            throw new IllegalStateException("The chosen creature cannot receive these counters");
        }
        if (gameQueryService.cantBeChosen(gameData, chosen)) {
            throw new IllegalStateException(FaceDownVisibility.publicName(gameData, chosen) + " can't be chosen");
        }
        CounterType type = cost.counterType();
        count = gameQueryService.replaceCounters(gameData, chosen, type, count, player.getId());
        if (count <= 0) {
            return;
        }
        chosen.setCounterCount(type, chosen.getCounterCount(type) + count);
        gameData.playersWhoPutCountersOnCreaturesThisTurn.add(player.getId());
        if (type == CounterType.PLUS_ONE_PLUS_ONE) {
            gameData.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn.add(player.getId());
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(player.getId());
            gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn.merge(
                    player.getId(), count, Integer::sum);
        }

        String counterWord = count == 1
                ? "a " + counterLabel() + " counter"
                : count + " " + counterLabel() + " counters";
        gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " puts " + counterWord + " on " , chosen.getCard(), "."));
    }

    @Override
    public String getPromptMessage(int remaining) {
        return "Choose a creature to put a " + counterLabel() + " counter on.";
    }

    private int count(GameData gameData, UUID playerId) {
        return amountEvaluationService.evaluate(gameData, cost.amount(),
                new AmountContext(playerId, null, null, xValue, 0));
    }

    private String counterLabel() {
        return switch (cost.counterType()) {
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case CHARGE -> "charge";
            default -> "";
        };
    }

    private boolean canReceiveCounters(GameData gameData, Permanent creature) {
        return !gameQueryService.cantHaveCounters(gameData, creature)
                && (cost.counterType() != CounterType.PLUS_ONE_PLUS_ONE
                || !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, creature))
                && (cost.counterType() != CounterType.MINUS_ONE_MINUS_ONE
                || !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, creature));
    }
}
