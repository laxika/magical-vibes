package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Sets the source permanent's base toughness to a dynamically evaluated value indefinitely.
 * The value is evaluated when the effect resolves and is then locked in.
 */
public record SetSelfBaseToughnessToAmountIndefinitelyEffect(DynamicAmount toughness)
        implements CardEffect {
}
