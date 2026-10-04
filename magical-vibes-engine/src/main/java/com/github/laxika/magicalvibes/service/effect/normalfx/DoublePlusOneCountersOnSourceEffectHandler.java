package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a source-bound effect that doubles the source's +1/+1 counters. */
@Component
@RequiredArgsConstructor
public class DoublePlusOneCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoublePlusOneCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        int current = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (current <= 0) {
            return;
        }

        int added = current * (MaroGoneNutsSupport.apply(gameData, effect, 2) - 1);
        added = gameQueryService.replaceCounters(gameData, source, CounterType.PLUS_ONE_PLUS_ONE,
                added, entry.getControllerId());
        if (added <= 0) return;
        source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, current + added);
        permanentCounterSupport.notifyCountersPlaced(gameData, entry, source, added,
                CounterType.PLUS_ONE_PLUS_ONE);
        permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                gameData, source, entry.getControllerId());
        permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, source,
                added,
                entry.getControllerId());
        permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, source, entry.getControllerId());
    }
}
