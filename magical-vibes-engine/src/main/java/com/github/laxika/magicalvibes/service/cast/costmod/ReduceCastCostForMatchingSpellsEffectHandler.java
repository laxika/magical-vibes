package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceCastCostForMatchingSpellsEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceCastCostForMatchingSpellsEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var reduce = (ReduceCastCostForMatchingSpellsEffect) effect;
        boolean applies = switch (reduce.scope()) {
            case SELF -> source.controlledBy(context.castingPlayerId());
            case OPPONENT -> !source.controlledBy(context.castingPlayerId());
        };
        if (!applies) {
            return 0;
        }
        return predicateEvaluationService.matchesCardPredicate(context.spell(), reduce.predicate(), null)
                ? -reduce.amount() : 0;
    }
}
