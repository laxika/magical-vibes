package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;
import java.util.UUID;

/** Conjures one duplicate per oil counter on the dying creature into its controller's library. */
public record ConjureDuplicatesOfDyingCreatureIntoLibraryEffect(
        UUID dyingCardId,
        int oilCounterCount
) implements CardEffect, DyingCreatureCardAwareEffect, DyingCreatureCountersAwareEffect {

    public ConjureDuplicatesOfDyingCreatureIntoLibraryEffect() {
        this(null, 0);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ConjureDuplicatesOfDyingCreatureIntoLibraryEffect(dyingCardId, oilCounterCount);
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new ConjureDuplicatesOfDyingCreatureIntoLibraryEffect(
                dyingCardId, counters.getOrDefault(CounterType.OIL, 0));
    }
}
