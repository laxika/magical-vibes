package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveAllCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/** Resolves the Ozolith-style move of all counters from the source onto its target creature. */
@Component
@RequiredArgsConstructor
public class MoveAllCountersFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveAllCountersFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (source == null || target == null) {
            return;
        }

        for (CounterType counterType : source.getCounters().keySet().stream().toList()) {
            int count = source.getCounterCount(counterType);
            source.setCounterCount(counterType, 0);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(source, count);
            }
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counterType, count);
        }
    }
}
