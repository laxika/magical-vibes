package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts counters on the source card's spell object while it is on the stack. */
public record PutCountersOnStackEntryCardEffect(CounterType counterType, int count) implements CardEffect {

    public PutCountersOnStackEntryCardEffect(CounterType counterType) {
        this(counterType, 1);
    }
}
