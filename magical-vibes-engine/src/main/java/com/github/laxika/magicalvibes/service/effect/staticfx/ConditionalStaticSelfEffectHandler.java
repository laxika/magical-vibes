package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generic self-only (characteristic-defining) static handler for {@link ConditionalEffect}:
 * evaluates the condition via {@link ConditionEvaluationService} and, when met, applies the
 * wrapped effect to the source itself via the shared self-application routine. Replaces the
 * former per-condition self handler classes.
 */
@Component
@RequiredArgsConstructor
public class ConditionalStaticSelfEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final ConditionEvaluationService conditionEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (!conditionEvaluationService.isMet(context.gameData(), conditional.condition(),
                ConditionContext.forStaticEffect(context.source(), controllerId))) {
            return;
        }
        support.applySelfOnlyConditionalStaticEffect(context, conditional.wrapped(), accumulator);
    }
}
