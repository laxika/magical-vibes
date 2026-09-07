package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/** Puts the counters a leaving creature had onto the permanent carrying the trigger. */
public record PutLeavingCreatureCountersOnSourceEffect(Map<CounterType, Integer> counters)
        implements CardEffect, LeavingCreatureCountersAwareEffect {

    public PutLeavingCreatureCountersOnSourceEffect {
        counters = Map.copyOf(counters);
    }

    public PutLeavingCreatureCountersOnSourceEffect() {
        this(Map.of());
    }

    @Override
    public CardEffect boundToLeavingCreatureCounters(Map<CounterType, Integer> counters) {
        return new PutLeavingCreatureCountersOnSourceEffect(counters);
    }
}
