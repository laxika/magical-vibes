package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put a fixed number of counters of the specified type on this permanent.
 * The plural counterpart of {@link PutCounterOnSelfEffect}, used when a single
 * resolution places more than one counter (e.g. Withengar Unbound's thirteen
 * +1/+1 counters).
 */
public record PutCountersOnSelfEffect(CounterType counterType, int count) implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
