package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts {@code count} counters of {@code counterType} on the creature the source Equipment is
 * attached to. Does nothing if the Equipment is unattached or the equipped creature has left the
 * battlefield.
 *
 * <p>{@code condition} is optional and checked against the equipped creature at resolution: if set
 * and the creature does not match, no counters are placed. This models the M13 Ring cycle's
 * "put a +1/+1 counter on equipped creature if it's blue" upkeep trigger.
 */
public record PutCountersOnEquippedCreatureEffect(
        CounterType counterType,
        int count,
        PermanentPredicate condition
) implements CardEffect {

    public PutCountersOnEquippedCreatureEffect(CounterType counterType, int count) {
        this(counterType, count, null);
    }
}
