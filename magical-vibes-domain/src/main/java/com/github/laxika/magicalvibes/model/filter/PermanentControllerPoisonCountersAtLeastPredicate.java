package com.github.laxika.magicalvibes.model.filter;

/** Matches permanents whose current controller has at least the requested number of poison counters. */
public record PermanentControllerPoisonCountersAtLeastPredicate(int minimumPoisonCounters)
        implements PermanentPredicate {
}
