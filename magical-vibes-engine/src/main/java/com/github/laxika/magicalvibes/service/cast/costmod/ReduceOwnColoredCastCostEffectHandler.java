package com.github.laxika.magicalvibes.service.cast.costmod;

import com.github.laxika.magicalvibes.model.ManaColor;
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

/**
 * Spell-self handler for {@link ReduceOwnColoredCastCostEffect}: the unit, times the evaluated amount, is a coloured
 * reduction of the printed cost. For a one-colour unit, the part no printed symbol could absorb also offsets generic
 * cost increases ({@link #modifyCostAfterOtherModifiers}); a mixed unit reduces only the printed cost.
 */
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
        ManaColor color = singleColor(reduce.unit());
        ManaCost printedCost = context.spell().getParsedManaCost();
        if (color == null || printedCost == null) {
            return 0;
        }
        int amount = evaluateAmount(context, reduce) * reduce.unit().getManaValue();
        int genericReductionRemainder = amount
                - printedCost.countColorSymbols(color)
                - printedCost.getGenericCost();
        int coveredPrintedMana = Math.min(amount,
                printedCost.countColorSymbols(color) + printedCost.getGenericCost());
        return -Math.min(Math.max(0, genericReductionRemainder),
                Math.max(0, accumulatedModifier + coveredPrintedMana));
    }

    @Override
    public ManaCost coloredManaCostReduction(CostModificationContext context, CardEffect effect,
                                             CostModificationSource source) {
        var reduce = (ReduceOwnColoredCastCostEffect) effect;
        int amount = evaluateAmount(context, reduce);
        if (amount <= 0) {
            return null;
        }
        return new ManaCost(reduce.unit().toManaCostString().repeat(amount));
    }

    @Override
    public boolean coloredReductionCanReduceGeneric() {
        return true;
    }

    private int evaluateAmount(CostModificationContext context, ReduceOwnColoredCastCostEffect effect) {
        return amountEvaluationService.evaluate(context.gameData(), effect.amount(),
                AmountContext.forCasting(context.castingPlayerId()));
    }

    /** The unit's one colour when it is only symbols of that colour ({U}, {G}{G}), else {@code null}. */
    private static ManaColor singleColor(ManaCost unit) {
        if (unit.getGenericCost() != 0) {
            return null;
        }
        for (ManaColor color : ManaColor.values()) {
            if (unit.getManaValue() > 0 && unit.countColorSymbols(color) == unit.getManaValue()) {
                return color;
            }
        }
        return null;
    }
}
