package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/** Resolves a choice to remove one counter from each of exactly two controlled creatures. */
public record RemoveCounterFromTwoCreaturesThenEffect(CounterType counterType, CardEffect thenEffect)
        implements CardEffect {

    public RemoveCounterFromTwoCreaturesThenEffect {
        Objects.requireNonNull(counterType, "counterType");
        Objects.requireNonNull(thenEffect, "thenEffect");
    }
}
