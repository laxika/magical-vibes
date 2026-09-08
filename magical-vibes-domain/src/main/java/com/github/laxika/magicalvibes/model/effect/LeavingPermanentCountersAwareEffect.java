package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Map;

/**
 * Capability for a leave-the-battlefield trigger that needs the departing permanent's counters.
 * The trigger collector binds the snapshot before the permanent is gone from the battlefield.
 */
public interface LeavingPermanentCountersAwareEffect {

    /**
     * Returns the effect with the departing permanent's counter snapshot bound in, or {@code null}
     * when the trigger has no effect for that snapshot.
     */
    CardEffect boundToLeavingPermanentCounters(Map<CounterType, Integer> counters);
}
