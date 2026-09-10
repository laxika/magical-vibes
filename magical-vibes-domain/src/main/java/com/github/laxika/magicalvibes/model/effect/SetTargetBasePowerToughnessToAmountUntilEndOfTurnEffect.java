package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Sets a target creature's base power and toughness to dynamically evaluated values until end of
 * turn.
 */
public record SetTargetBasePowerToughnessToAmountUntilEndOfTurnEffect(
        DynamicAmount power,
        DynamicAmount toughness) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
