package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnColoredCastCostEffect;
import com.github.laxika.magicalvibes.service.cast.CostModificationContext;
import com.github.laxika.magicalvibes.service.cast.CostModificationHandlerBean;
import com.github.laxika.magicalvibes.service.cast.CostModificationSource;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReduceOwnColoredCastCostEffectHandler implements CostModificationHandlerBean {

    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReduceOwnColoredCastCostEffect.class;
    }

    @Override
    public boolean onSpellItself() {
        return true;
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
    public int modifyCostAfterOtherModifiers(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source, int accumulatedModifier) {
        var reduce = (ReduceOwnColoredCastCostEffect) effect;
        int amount = evaluateAmount(context, reduce);
        ManaCost printedCost = context.spell().getParsedManaCost();
        if (printedCost == null) {
            return 0;
        }
        int genericReductionRemainder = amount
                - printedCost.countColorSymbols(reduce.color())
                - printedCost.getGenericCost();
        return -Math.min(Math.max(0, genericReductionRemainder), Math.max(0, accumulatedModifier));
    }

    @Override
    public ManaCost coloredManaCostReduction(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source) {
        var reduce = (ReduceOwnColoredCastCostEffect) effect;
        int amount = evaluateAmount(context, reduce);
        if (amount <= 0) {
            return null;
        }
        return new ManaCost(("{" + reduce.color().getCode() + "}").repeat(amount));
    }

    @Override
    public boolean coloredReductionCanReduceGeneric() {
        return true;
    }

    private int evaluateAmount(CostModificationContext context, ReduceOwnColoredCastCostEffect effect) {
        return amountEvaluationService.evaluate(context.gameData(), effect.amount(),
                AmountContext.forCasting(context.castingPlayerId()));
    }
}
