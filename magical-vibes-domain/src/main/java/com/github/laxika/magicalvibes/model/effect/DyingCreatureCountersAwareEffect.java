package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Capability for an ally-creature-death effect that needs the dying creature's concrete counters.
 * The death trigger pipeline binds the counter snapshot before putting the effect on the stack.
 */
public interface DyingCreatureCountersAwareEffect {

    /** Returns the effect with the dying creature's counter snapshot bound in. */
    CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters);
}
