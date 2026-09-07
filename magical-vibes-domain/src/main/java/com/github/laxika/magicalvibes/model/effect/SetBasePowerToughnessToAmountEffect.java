package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Continuous effect that sets the base power and toughness of creatures in a scope to
 * dynamically evaluated amounts.
 */
public record SetBasePowerToughnessToAmountEffect(
        DynamicAmount power,
        DynamicAmount toughness,
        GrantScope scope
) implements CardEffect {
}
