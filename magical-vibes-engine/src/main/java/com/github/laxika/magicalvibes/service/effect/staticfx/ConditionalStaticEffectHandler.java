package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandler;
import com.github.laxika.magicalvibes.service.effect.StaticEffectHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Generic static handler for {@link ConditionalEffect}: evaluates the condition via
 * {@link ConditionEvaluationService} and, when met, delegates to the wrapped effect's own
 * registered static handler. Replaces the former per-condition handler classes
 * (metalcraft, controller-turn, life-threshold, …) whose class count multiplied as
 * conditions × contexts.
 */
@Component
@RequiredArgsConstructor
public class ConditionalStaticEffectHandler implements StaticEffectHandlerBean {

    private final StaticEffectSupport support;
    private final ConditionEvaluationService conditionEvaluationService;
    private final StaticEffectHandlerRegistry registry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        var conditional = (ConditionalEffect) effect;
        UUID controllerId = support.findControllerId(context.gameData(), context.source());
        if (!conditionEvaluationService.isMet(context.gameData(), conditional.condition(),
                ConditionContext.forStaticEffect(context.source(), controllerId))) {
            return;
        }
        CardEffect wrapped = conditional.wrapped();
        StaticEffectHandler handler = registry.getHandler(wrapped);
        if (handler != null) {
            handler.apply(context, wrapped, accumulator);
        }
    }
}
