package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Put one or more counters of the specified type on this permanent. The count is either a fixed
 * {@code count} or, when {@code amount} is non-null, a {@link DynamicAmount} resolved at
 * resolution time (e.g. {@code XValue} for "{X}: Put X tower counters on this enchantment").
 */
public record PutCountersOnSelfEffect(CounterType counterType, int count, DynamicAmount amount) implements CardEffect {

    public PutCountersOnSelfEffect(CounterType counterType) {
        this(counterType, 1, null);
    }

    public PutCountersOnSelfEffect(CounterType counterType, int count) {
        this(counterType, count, null);
    }

    public PutCountersOnSelfEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, 0, amount);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
