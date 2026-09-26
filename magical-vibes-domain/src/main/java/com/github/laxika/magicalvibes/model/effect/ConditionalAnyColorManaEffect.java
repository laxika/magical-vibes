package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Condition;

/** Adds one of any color, with the amount selected by a condition at resolution. */
public record ConditionalAnyColorManaEffect(Condition condition, DynamicAmount ifMetAmount,
                                             DynamicAmount ifNotMetAmount)
        implements ManaProducingEffect {

    public ConditionalAnyColorManaEffect(Condition condition, int ifMetAmount, int ifNotMetAmount) {
        this(condition, new Fixed(ifMetAmount), new Fixed(ifNotMetAmount));
    }

    @Override
    public DynamicAmount estimatedManaAmount() {
        return ifMetAmount;
    }

    @Override
    public boolean estimatedCountsAllColors() {
        return ifMetAmount instanceof Fixed && ifNotMetAmount instanceof Fixed;
    }

    @Override
    public int estimatedWildcardMana() {
        if (ifMetAmount instanceof Fixed met && ifNotMetAmount instanceof Fixed notMet) {
            return Math.max(met.value(), notMet.value());
        }
        return 0;
    }
}
