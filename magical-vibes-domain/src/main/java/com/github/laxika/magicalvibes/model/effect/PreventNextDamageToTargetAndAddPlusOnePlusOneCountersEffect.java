package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Prevents the next amount of damage to target creature this turn and puts a +1/+1 counter on
 * that creature for each point of damage prevented.
 */
public record PreventNextDamageToTargetAndAddPlusOnePlusOneCountersEffect(DynamicAmount amount)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
