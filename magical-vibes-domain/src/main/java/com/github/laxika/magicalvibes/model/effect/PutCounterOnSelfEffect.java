package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put a counter of the specified type on this permanent.
 * Supports any {@link CounterType} except {@code ANY}.
 */
public record PutCounterOnSelfEffect(CounterType counterType) implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
