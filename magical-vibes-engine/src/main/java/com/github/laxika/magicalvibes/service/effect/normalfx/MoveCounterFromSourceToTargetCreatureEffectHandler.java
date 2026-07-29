package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MoveCounterFromSourceToTargetCreatureEffect}: removes one counter of the given type
 * from the source permanent and places it on the target creature. Nothing is placed when the source
 * left the battlefield or no longer has such a counter.
 */
@Component
@RequiredArgsConstructor
public class MoveCounterFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveCounterFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CounterType counterType = ((MoveCounterFromSourceToTargetCreatureEffect) effect).counterType();

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null || source.getCounterCount(counterType) <= 0) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, 1);
    }
}
