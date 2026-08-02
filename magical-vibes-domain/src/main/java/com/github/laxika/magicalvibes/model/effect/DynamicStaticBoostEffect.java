package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Continuous P/T boost whose amounts are evaluated from the source permanent and its controller.
 * This is the dynamic counterpart to {@link StaticBoostEffect} for anthem effects such as
 * "+0/+1 for each Gate you control".
 */
public record DynamicStaticBoostEffect(
        DynamicAmount powerBoost,
        DynamicAmount toughnessBoost,
        GrantScope scope,
        PermanentPredicate filter
) implements CardEffect {

    public DynamicStaticBoostEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost,
                                    GrantScope scope) {
        this(powerBoost, toughnessBoost, scope, null);
    }
}
