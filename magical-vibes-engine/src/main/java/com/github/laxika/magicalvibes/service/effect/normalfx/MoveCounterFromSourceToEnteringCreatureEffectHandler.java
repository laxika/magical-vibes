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
 * Resolves a graft-style counter move from the source permanent to the recorded entering
 * permanent without treating that permanent as a target.
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
        var move = (MoveCounterFromSourceToEnteringCreatureEffect) effect;
        CounterType counterType = move.counterType();

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent entering = gameQueryService.findPermanentById(gameData, move.enteringPermanentId());
        if (source == null || entering == null || source.getCounterCount(counterType) <= 0) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, 1);
        }
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, entering, counterType, 1);
    }
}
