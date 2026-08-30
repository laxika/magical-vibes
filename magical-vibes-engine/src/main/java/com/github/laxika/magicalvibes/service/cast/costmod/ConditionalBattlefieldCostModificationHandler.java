package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerRegistry;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import org.springframework.stereotype.Component;

/**
 * Evaluates a conditional cost modifier carried by a battlefield permanent and delegates the
 * active branch to the wrapped battlefield cost handler.
 */
@Component
public class ConditionalBattlefieldCostModificationHandler implements CostModificationHandlerBean {

    private final ConditionEvaluationService conditionEvaluationService;
    private final CostModificationHandlerRegistry costModificationHandlerRegistry;

    public ConditionalBattlefieldCostModificationHandler(
            ConditionEvaluationService conditionEvaluationService,
            CostModificationHandlerRegistry costModificationHandlerRegistry) {
        this.conditionEvaluationService = conditionEvaluationService;
        this.costModificationHandlerRegistry = costModificationHandlerRegistry;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConditionalEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        if (source.sourcePermanent() == null
                || !conditionEvaluationService.isMet(context.gameData(), ((ConditionalEffect) effect).condition(),
                ConditionContext.forStaticEffect(source.sourcePermanent(), source.controllerId()))) {
            return 0;
        }
        CardEffect wrapped = ((ConditionalEffect) effect).wrapped();
        CostModificationHandlerBean handler = costModificationHandlerRegistry.getBattlefieldHandler(wrapped);
        return handler == null ? 0 : handler.modifyCost(context, wrapped, source);
    }
}
