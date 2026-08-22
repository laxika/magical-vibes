package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseSpellCostEffect;
import com.github.laxika.magicalvibes.model.ManaCost;
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
public class IncreaseSpellCostEffectHandler implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return IncreaseSpellCostEffect.class;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        var increase = (IncreaseSpellCostEffect) effect;
        if (!applies(context, increase, source) || increase.amount() == null) {
            return 0;
        }
        var amountContext = AmountContext.forStaticEffect(source.sourcePermanent(),
                context.castingPlayerId());
        return amountEvaluationService.evaluate(context.gameData(), increase.amount(), amountContext);
    }

    @Override
    public ManaCost coloredManaCostIncrease(CostModificationContext context, CardEffect effect,
                                            CostModificationSource source) {
        var increase = (IncreaseSpellCostEffect) effect;
        if (!applies(context, increase, source) || increase.manaCost() == null) {
            return null;
        }
        return new ManaCost(increase.manaCost());
    }

    private boolean applies(CostModificationContext context, IncreaseSpellCostEffect increase,
                            CostModificationSource source) {
        boolean inScope = switch (increase.scope()) {
            case SELF -> source.controlledBy(context.castingPlayerId());
            case OPPONENT -> !source.controlledBy(context.castingPlayerId());
            case ALL -> true;
        };
        if (!inScope) {
            return false;
        }
        return predicateEvaluationService.matchesCardPredicate(context.spell(), increase.predicate(), null);
    }
}
