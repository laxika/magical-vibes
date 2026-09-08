package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Puts counters on the permanent identified by the current stack entry's chosen permanent. */
public record PutCountersOnChosenPermanentEffect(CounterType counterType, DynamicAmount amount)
        implements CardEffect {

    public PutCountersOnChosenPermanentEffect(CounterType counterType, int count) {
        this(counterType, new Fixed(count));
    }
}
