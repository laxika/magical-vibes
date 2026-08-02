package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Activation cost: put {@code count} counters of {@code counterType} on the source permanent, paid
 * immediately on activation. The typed sibling of {@link PutCounterOnSourceCost}, which only models
 * P/T-modifying counters. Used by Yisan, the Wanderer Bard ("Put a verse counter on Yisan: ...").
 */
public record PutTypedCounterOnSourceCost(CounterType counterType, int count) implements CostEffect {

    public PutTypedCounterOnSourceCost(CounterType counterType) {
        this(counterType, 1);
    }
}
