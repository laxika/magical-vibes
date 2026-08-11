package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/** Matches permanents with at least {@code threshold} counters of {@code counterType}. */
public record PermanentCounterCountAtLeastPredicate(CounterType counterType, int threshold)
        implements PermanentPredicate {
}
