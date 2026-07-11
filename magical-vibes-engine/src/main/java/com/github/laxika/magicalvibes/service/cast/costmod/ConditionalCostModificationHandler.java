package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerRegistry;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Generic cost-modifier unwrapper for {@link ConditionalEffect} on the spell being cast:
 * evaluates the condition via {@link ConditionEvaluationService} against a cast-time
 * {@link ConditionContext} and, when met, delegates to the wrapped effect's own registered
 * spell-self handler. Mirrors {@code ConditionalStaticEffectHandler}; replaces the former
 * per-condition {@code ReduceOwnCastCostIf*} handler classes ("costs {N} less to cast if …").
 */
@Component
@RequiredArgsConstructor
public class ConditionalCostModificationHandler implements CostModificationHandlerBean {

    private final ConditionEvaluationService conditionEvaluationService;
    private final CostModificationHandlerRegistry costModificationHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var conditional = (ConditionalEffect) effect;
        if (!conditionEvaluationService.isMet(context.gameData(), conditional.condition(),
                ConditionContext.forCasting(context.castingPlayerId()))) {
            return 0;
        }
        CardEffect wrapped = conditional.wrapped();
        CostModificationHandlerBean handler = costModificationHandlerRegistry.getSpellSelfHandler(wrapped);
        return handler == null ? 0 : handler.modifyCost(context, wrapped, source);
    }
}
