package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost effect that removes counters of the specified type from among creatures you control.
 * Unlike {@link RemoveCounterFromSourceCost}, this allows removing counters from ANY creature
 * the player controls (including the source). When {@code count} &gt; 1, counters may be split
 * across creatures ("Remove two +1/+1 counters from among creatures you control").
 *
 * @param count number of counters to remove in total
 * @param counterType type of counter to remove
 */
public record RemoveCounterFromControlledCreatureCost(int count, CounterType counterType) implements CostEffect {
}
