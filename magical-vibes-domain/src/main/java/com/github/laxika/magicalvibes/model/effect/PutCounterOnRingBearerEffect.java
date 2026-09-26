package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts counters on the controller's currently designated Ring-bearer. */
public record PutCounterOnRingBearerEffect(CounterType counterType, int amount) implements CardEffect {

    public PutCounterOnRingBearerEffect(CounterType counterType) {
        this(counterType, 1);
    }
}
