package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveDyingSourceCountersToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MoveDyingSourceCountersToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MoveDyingSourceCountersToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MoveDyingSourceCountersToTargetCreatureEffect) effect;
        Permanent target = e.subject().declaresTarget()
                ? gameQueryService.findPermanentById(gameData, entry.getTargetId())
                : subjectResolutionService.permanent(gameData, entry, effect, e.subject());
        if (target == null) {
            return;
        }

        for (Map.Entry<CounterType, Integer> counter : entry.triggeringPermanentCounters().entrySet()) {
            permanentCounterSupport.placeCounterOnPermanent(gameData, entry, target, counter.getKey(), counter.getValue());
        }
    }
}
