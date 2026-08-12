package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts counters on the battlefield permanent represented by the ability's source card. */
public record PutCountersOnSourceCardEffect(CounterType counterType, int count) implements CardEffect {

    public PutCountersOnSourceCardEffect(CounterType counterType) {
        this(counterType, 1);
    }
}
