package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a spell or ability whose controller has at least {@code minimumPoisonCounters} poison counters: "if its
 * controller is poisoned" (1), "if its controller has three or more poison counters" (3).
 */
public record StackEntryControllerPoisonCountersAtLeastPredicate(int minimumPoisonCounters)
        implements StackEntryPredicate {

    public StackEntryControllerPoisonCountersAtLeastPredicate {
        if (minimumPoisonCounters < 1) {
            throw new IllegalArgumentException("minimumPoisonCounters must be at least 1");
        }
    }
}
