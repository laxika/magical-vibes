package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/** Capability for an ally-creature-leaves trigger that needs the leaving creature's counters. */
public interface LeavingCreatureCountersAwareEffect {

    /** Returns the effect with the leaving creature's counter snapshot bound in. */
    CardEffect boundToLeavingCreatureCounters(Map<CounterType, Integer> counters);
}
