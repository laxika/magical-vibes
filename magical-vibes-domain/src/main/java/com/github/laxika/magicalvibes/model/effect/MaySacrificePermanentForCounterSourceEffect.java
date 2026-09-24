package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice a permanent matching {@code filter}. If you do, put counters on this source."
 * A decline has no effect. The default counter type is +1/+1 for creature effects that use this
 * pattern; named counters can be supplied for cards such as Chitterspitter.
 */
public record MaySacrificePermanentForCounterSourceEffect(
        PermanentPredicate filter,
        String description,
        DynamicAmount counterAmount,
        CounterType counterType
) implements CardEffect {

    public MaySacrificePermanentForCounterSourceEffect(PermanentPredicate filter, String description) {
        this(filter, description, new Fixed(1), CounterType.PLUS_ONE_PLUS_ONE);
    }

    public MaySacrificePermanentForCounterSourceEffect(PermanentPredicate filter, String description,
                                                       DynamicAmount counterAmount) {
        this(filter, description, counterAmount, CounterType.PLUS_ONE_PLUS_ONE);
    }

    public MaySacrificePermanentForCounterSourceEffect(PermanentPredicate filter, String description,
                                                       CounterType counterType) {
        this(filter, description, new Fixed(1), counterType);
    }

    public MaySacrificePermanentForCounterSourceEffect {
        if (counterAmount == null) {
            throw new IllegalArgumentException("Counter amount must not be null");
        }
        if (counterType == null) {
            throw new IllegalArgumentException("Counter type must not be null");
        }
    }
}
