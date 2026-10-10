package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEqualToSourceCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/** Resolves a Denry Klin-style copy of all source counter counts onto the entering creature. */
@Component
@RequiredArgsConstructor
public class PutCountersOnEnteringCreatureEqualToSourceCountersEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnEnteringCreatureEqualToSourceCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID enteringPermanentId = entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId() : entry.getTargetId();
        Permanent enteringCreature = gameQueryService.findPermanentById(gameData, enteringPermanentId);
        if (source == null) {
            // Last known information: the source left the battlefield while the trigger was on the stack.
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null || enteringCreature == null) {
            return;
        }

        Map<CounterType, Integer> counters = Map.copyOf(source.getCounters());
        for (Map.Entry<CounterType, Integer> counter : counters.entrySet()) {
            if (counter.getValue() > 0) {
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, enteringCreature, counter.getKey(), counter.getValue());
            }
        }
    }
}
