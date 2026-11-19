package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoublePlusOneCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link DoublePlusOneCountersOnTargetCreatureEffect}: each named permanent with +1/+1 counters gets as many
 * more (CR 701.10e), placed through {@link PermanentCounterSupport#placeCounterOnPermanent}, so can't-have-counters,
 * counter replacements and the counter-placement triggers apply as for any other placement. A target that is no
 * longer a creature is not affected (CR 608.2b).
 */
@Component
@RequiredArgsConstructor
public class DoublePlusOneCountersOnTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DoublePlusOneCountersOnTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var doubling = (DoublePlusOneCountersOnTargetCreatureEffect) effect;
        for (Permanent permanent : doubledPermanents(gameData, entry, doubling)) {
            int current = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
            if (current > 0) {
                permanentCounterSupport.placeCounterOnPermanent(gameData, entry, permanent,
                        CounterType.PLUS_ONE_PLUS_ONE, current * MaroGoneNutsSupport.apply(gameData, effect, 2) - current);
            }
        }
    }

    private List<Permanent> doubledPermanents(GameData gameData, StackEntry entry,
                                              DoublePlusOneCountersOnTargetCreatureEffect effect) {
        if (!(effect.subject() instanceof PermanentSubject.Target)) {
            return subjectResolutionService.permanents(gameData, entry, effect, effect.subject());
        }
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                targets.add(target);
            }
        }
        return targets;
    }
}
