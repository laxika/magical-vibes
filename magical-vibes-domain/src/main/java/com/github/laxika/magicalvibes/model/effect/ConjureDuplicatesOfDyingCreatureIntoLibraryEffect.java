package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Conjures one duplicate per oil counter on the dying creature into its controller's library. The creature's card
 * is the stack entry's triggering card.
 */
public record ConjureDuplicatesOfDyingCreatureIntoLibraryEffect(int oilCounterCount)
        implements CardEffect, DyingCreatureCountersAwareEffect {

    public ConjureDuplicatesOfDyingCreatureIntoLibraryEffect() {
        this(0);
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new ConjureDuplicatesOfDyingCreatureIntoLibraryEffect(counters.getOrDefault(CounterType.OIL, 0));
    }
}
