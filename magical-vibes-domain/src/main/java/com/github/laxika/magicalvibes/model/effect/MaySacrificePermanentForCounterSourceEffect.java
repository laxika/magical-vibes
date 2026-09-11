package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice a permanent matching {@code filter}. If you do, put counters on this creature."
 * A decline has no effect.
 */
public record MaySacrificePermanentForCounterSourceEffect(
        PermanentPredicate filter,
        String description,
        DynamicAmount counterAmount
) implements CardEffect {

    public MaySacrificePermanentForCounterSourceEffect(PermanentPredicate filter, String description) {
        this(filter, description, new Fixed(1));
    }

    public MaySacrificePermanentForCounterSourceEffect {
        if (counterAmount == null) {
            throw new IllegalArgumentException("Counter amount must not be null");
        }
    }
}
