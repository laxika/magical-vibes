package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Moves every concrete counter from a dying creature onto the source permanent of the death
 * trigger. The death-trigger collector binds the dying creature's counter snapshot before the
 * effect is put on the stack.
 *
 * @param counters snapshot of the dying creature's counters
 */
public record MoveDyingSourceCountersToSourceEffect(Map<CounterType, Integer> counters)
        implements CardEffect, DyingCreatureCountersAwareEffect {

    public MoveDyingSourceCountersToSourceEffect {
        counters = Map.copyOf(counters);
    }

    public MoveDyingSourceCountersToSourceEffect() {
        this(Map.of());
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new MoveDyingSourceCountersToSourceEffect(counters);
    }

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
