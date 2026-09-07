package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Doubles the number of counters of the specified type on the source permanent. */
public record DoubleCountersOnSourceEffect(CounterType counterType) implements CardEffect {
}
