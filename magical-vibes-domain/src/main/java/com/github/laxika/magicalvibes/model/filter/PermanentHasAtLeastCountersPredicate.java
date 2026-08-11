package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/** Matches permanents with at least the requested number of counters of one type. */
public record PermanentHasAtLeastCountersPredicate(CounterType counterType, int minimum)
        implements PermanentPredicate {
}
