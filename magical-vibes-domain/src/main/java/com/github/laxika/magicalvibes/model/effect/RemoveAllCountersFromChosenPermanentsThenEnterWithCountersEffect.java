package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As this permanent enters, the controller chooses any number of matching permanents, removes
 * every counter from them, and makes the entering permanent receive the configured number of
 * counters for each counter removed.
 */
public record RemoveAllCountersFromChosenPermanentsThenEnterWithCountersEffect(
        PermanentPredicate filter, CounterType counterType, int multiplier) implements ReplacementEffect {
}
