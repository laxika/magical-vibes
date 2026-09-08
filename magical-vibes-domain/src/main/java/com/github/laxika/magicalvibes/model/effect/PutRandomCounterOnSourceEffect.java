package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.List;

/** Puts one randomly selected counter type that the source permanent does not have on it. */
public record PutRandomCounterOnSourceEffect(List<CounterType> counterTypes) implements CardEffect {

    public PutRandomCounterOnSourceEffect {
        if (counterTypes == null || counterTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one counter type is required");
        }
        if (counterTypes.stream().distinct().count() != counterTypes.size()) {
            throw new IllegalArgumentException("Counter types must be distinct");
        }
        counterTypes = List.copyOf(counterTypes);
    }
}
