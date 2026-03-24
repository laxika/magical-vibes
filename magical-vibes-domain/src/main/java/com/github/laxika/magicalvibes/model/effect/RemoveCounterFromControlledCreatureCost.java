package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost effect that removes a counter of the specified type from a creature you control.
 * Unlike {@link RemoveCounterFromSourceCost}, this allows removing a counter from ANY creature
 * the player controls, not just the source permanent.
 *
 * @param count number of counters to remove
 * @param counterType type of counter to remove
 */
public record RemoveCounterFromControlledCreatureCost(int count, CounterType counterType) implements CostEffect {
}
