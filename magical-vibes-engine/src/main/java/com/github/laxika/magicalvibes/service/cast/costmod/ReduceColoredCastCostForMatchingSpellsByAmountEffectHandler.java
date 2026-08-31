package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceColoredCastCostForMatchingSpellsByAmountEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles dynamic colored reductions applying to matching battlefield spells. */
@Component
@RequiredArgsConstructor
public class ReduceColoredCastCostForMatchingSpellsByAmountEffectHandler
        implements CostModificationHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceColoredCastCostForMatchingSpellsByAmountEffect.class;
    }

    @Override
    public boolean appliesAfterOtherCostModifiers() {
        return true;
    }

    @Override
    public int modifyCost(CostModificationContext context, CardEffect effect, CostModificationSource source) {
        return 0;
    }

    @Override
    public ManaCost coloredManaCostReduction(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source) {
        var reduce = (ReduceColoredCastCostForMatchingSpellsByAmountEffect) effect;
        boolean applies = switch (reduce.scope()) {
            case SELF -> source.controlledBy(context.castingPlayerId());
            case OPPONENT -> !source.controlledBy(context.castingPlayerId());
            case ALL -> true;
        };
        if (!applies || !predicateEvaluationService.matchesCardPredicate(
                context.spell(), reduce.predicate(),
                source.sourcePermanent() == null ? null : source.sourcePermanent().getCard().getId(),
                context.gameData(), context.castingPlayerId())) {
            return null;
        }

        int amount = amountEvaluationService.evaluate(context.gameData(), reduce.amount(),
                AmountContext.forStaticEffect(source.sourcePermanent(), context.castingPlayerId()));
        return amount <= 0 ? null : new ManaCost(("{" + reduce.color().getCode() + "}").repeat(amount));
    }

    @Override
    public boolean coloredReductionCanReduceGeneric(CardEffect effect) {
        return true;
    }
}
