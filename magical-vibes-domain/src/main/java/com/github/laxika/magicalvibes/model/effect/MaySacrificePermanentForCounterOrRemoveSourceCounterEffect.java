package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "You may sacrifice a permanent matching {@code filter}. If you do, put a counter on this
 * creature. If you don't, remove a counter from this creature."
 */
public record MaySacrificePermanentForCounterOrRemoveSourceCounterEffect(
        PermanentPredicate filter,
        CounterType counterType,
        String description
) implements CardEffect {
}
