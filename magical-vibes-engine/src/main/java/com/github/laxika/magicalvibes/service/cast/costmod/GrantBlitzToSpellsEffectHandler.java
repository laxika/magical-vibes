package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.BlitzGrantingEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantBlitzToSpellsEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GrantBlitzToSpellsEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantBlitzToSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public int modifyAlternateCost(CostModificationContext context, CardEffect effect,
                                   CostModificationSource source) {
        if (!context.blitzCost() || !source.controlledBy(context.castingPlayerId())
                || !(effect instanceof BlitzGrantingEffect grant)
                || !predicateEvaluationService.matchesCardPredicate(
                context.spell(), grant.blitzGrantFilter(), source.sourceCardId(),
                context.gameData(), context.castingPlayerId())) {
            return 0;
        }
        return -amountEvaluationService.evaluate(context.gameData(), grant.blitzCostReduction(),
                new AmountContext(context.castingPlayerId(), source.sourcePermanent(), null, 0, 0));
    }
}
