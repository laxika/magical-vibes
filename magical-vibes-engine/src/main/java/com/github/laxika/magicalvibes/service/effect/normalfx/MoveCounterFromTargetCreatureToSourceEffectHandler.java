package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MoveCounterFromTargetCreatureToSourceEffect}: removes one counter of the given
 * type from the target creature and places it on the source permanent.
 */
@Component
@RequiredArgsConstructor
public class MoveCounterFromTargetCreatureToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromTargetCreatureToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterType counterType = ((MoveCounterFromTargetCreatureToSourceEffect) effect).counterType();

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (target == null || source == null || target.getCounterCount(counterType) <= 0) {
            return;
        }

        target.setCounterCount(counterType, target.getCounterCount(counterType) - 1);
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, source, counterType, 1);
    }
}
