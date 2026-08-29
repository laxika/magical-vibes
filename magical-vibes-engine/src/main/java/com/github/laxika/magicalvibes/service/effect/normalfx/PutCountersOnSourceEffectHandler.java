package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCountersOnSourceEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        String counterLabel = String.format("%+d/%+d", e.powerModifier(), e.toughnessModifier());
        boolean plusZeroPlusOne = e.powerModifier() == 0 && e.toughnessModifier() > 0;
        int amount = e.amount();
        if (e.powerModifier() > 0) {
            if (gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) return;
            amount = gameQueryService.replaceCounters(gameData, source, CounterType.PLUS_ONE_PLUS_ONE,
                    amount, entry.getControllerId());
            if (amount <= 0) return;
            source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + amount);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, source, amount);
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, source);
        } else if (plusZeroPlusOne) {
            amount = gameQueryService.replaceCounters(gameData, source, CounterType.PLUS_ZERO_PLUS_ONE,
                    amount, entry.getControllerId());
            if (amount <= 0) return;
            source.setCounterCount(CounterType.PLUS_ZERO_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE) + amount);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, source, amount);
        } else {
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, source)) return;
            amount = gameQueryService.replaceCounters(gameData, source, CounterType.MINUS_ONE_MINUS_ONE,
                    amount, entry.getControllerId());
            if (amount <= 0) return;
            source.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + amount);
            permanentCounterSupport.notifyCountersPlaced(gameData, entry, source, amount);
        }
        gameLogService.append(gameData, GameLog.builder().card(source.getCard()).text(" gets " + amount + " " + counterLabel + " counter(s).").build());
        log.info("Game {} - {} gets {} {} counter(s)", gameData.id, source.getCard().getName(), amount, counterLabel);
        permanentCounterSupport.recordCounterPlacedOnCreature(gameData, source, entry.getControllerId());

        if (e.powerModifier() > 0) {
            permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                    gameData, source, amount, entry.getControllerId());
        } else if (!plusZeroPlusOne) {
            permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, source, amount);
        }
    }
}
