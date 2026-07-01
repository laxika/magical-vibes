package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put one or more counters of the specified type on this permanent.
 */
public record PutCountersOnSelfEffect(CounterType counterType, int count) implements CardEffect {

    public PutCountersOnSelfEffect(CounterType counterType) {
        this(counterType, 1);
    }

    @Override
    public boolean isSelfTargeting() { return true; }
}
