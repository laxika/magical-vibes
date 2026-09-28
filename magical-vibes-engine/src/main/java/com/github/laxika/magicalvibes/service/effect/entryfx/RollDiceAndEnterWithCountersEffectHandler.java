package com.github.laxika.magicalvibes.service.effect.entryfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollDiceAndEnterWithCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EntryReplacementHandlerBean;
import com.github.laxika.magicalvibes.service.effect.normalfx.DiceRollService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RollDiceAndEnterWithCountersEffectHandler implements EntryReplacementHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final DiceRollService diceRollService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollDiceAndEnterWithCountersEffect.class;
    }

    @Override
    public void apply(GameData gameData, UUID controllerId, Permanent enteringPermanent,
                      CardEffect effect, int xValue) {
        RollDiceAndEnterWithCountersEffect rollEffect = (RollDiceAndEnterWithCountersEffect) effect;
        int diceCount = amountEvaluationService.evaluate(gameData, rollEffect.diceCount(),
                AmountContext.forEnteringPermanent(controllerId, enteringPermanent, xValue));
        if (diceCount <= 0) {
            return;
        }

        List<Integer> results = new ArrayList<>(diceCount);
        int total = 0;
        for (int i = 0; i < diceCount; i++) {
            int result = diceRollService.roll(rollEffect.sides());
            results.add(result);
            total += result;
        }

        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(controllerId)
                + " rolls " + diceCount + " d" + rollEffect.sides() + " for "
                + enteringPermanent.getCard().getName() + ": " + results + "."));
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, controllerId, diceCount);
        triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                gameData, controllerId, rollEffect.sides(),
                results.stream().mapToInt(Integer::intValue).toArray());

        applyCounters(gameData, controllerId, enteringPermanent, rollEffect.counterType(), total);
    }

    private void applyCounters(GameData gameData, UUID controllerId, Permanent permanent,
                               CounterType counterType, int count) {
        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) {
            return;
        }
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
            return;
        }
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent, controllerId)) {
            return;
        }
        count = gameQueryService.replaceCounters(gameData, permanent, controllerId, counterType, count);
        if (count > 0) {
            permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) + count);
        }
    }
}
