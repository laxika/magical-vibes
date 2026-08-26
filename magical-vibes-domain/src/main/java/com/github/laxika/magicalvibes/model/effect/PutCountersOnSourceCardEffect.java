package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Puts counters on the battlefield permanent represented by the ability's source card. */
public record PutCountersOnSourceCardEffect(CounterType counterType, int count, DynamicAmount amount)
        implements CardEffect {

    public PutCountersOnSourceCardEffect(CounterType counterType) {
        this(counterType, 1, null);
    }

    public PutCountersOnSourceCardEffect(CounterType counterType, int count) {
        this(counterType, count, null);
    }

    public PutCountersOnSourceCardEffect(CounterType counterType, DynamicAmount amount) {
        this(counterType, 0, amount);
    }
}
