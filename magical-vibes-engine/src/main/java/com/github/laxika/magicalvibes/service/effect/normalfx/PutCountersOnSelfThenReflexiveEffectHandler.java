package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfThenReflexiveEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves counter placement followed by a successful reflexive ability. */
@Component
@RequiredArgsConstructor
public class PutCountersOnSelfThenReflexiveEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCountersOnSelfThenReflexiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCountersOnSelfThenReflexiveEffect counterThen =
                (PutCountersOnSelfThenReflexiveEffect) effect;
        UUID sourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        if (source == null) {
            return;
        }

        int placed = permanentCounterSupport.placeCounterOnPermanent(
                gameData, entry, source, counterThen.counterType(), counterThen.count());
        if (placed <= 0) {
            return;
        }
        if (counterThen.condition() != null
                && !conditionEvaluationService.isMet(gameData, counterThen.condition(),
                ConditionContext.forStackEntry(entry))) {
            return;
        }

        CardEffect reflexiveEffect = counterThen.condition() == null
                ? counterThen.reflexiveEffect()
                : new ConditionalEffect(counterThen.condition(), counterThen.reflexiveEffect());
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException(
                    "PutCountersOnSelfThenReflexiveEffect is not part of the resolving entry");
        }
        entry.insertEffectsToResolve(effectIndex + 1,
                List.of(new QueueReflexiveAbilityEffect(reflexiveEffect)));
    }
}
