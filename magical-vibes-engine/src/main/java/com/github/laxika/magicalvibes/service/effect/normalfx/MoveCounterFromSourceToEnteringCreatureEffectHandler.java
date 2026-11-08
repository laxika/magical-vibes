package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MoveCounterFromSourceToEnteringCreatureEffect} using the permanent bound by the
 * enter-the-battlefield trigger.
 */
@Component
@RequiredArgsConstructor
public class MoveCounterFromSourceToEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromSourceToEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterType counterType = ((MoveCounterFromSourceToEnteringCreatureEffect) effect).counterType();

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        // "That creature" is the stack entry's triggering permanent, where it is now; nothing if it has left.
        Permanent enteringCreature = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (source == null || enteringCreature == null || source.getCounterCount(counterType) <= 0) {
            return;
        }
        if (gameQueryService.cantHaveCounters(gameData, enteringCreature)
                || (counterType == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, enteringCreature))
                || (counterType == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, enteringCreature))) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, 1);
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, enteringCreature, counterType, 1);
    }
}
