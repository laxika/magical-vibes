package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Moves any number of one kind of counter from permanents controlled by the ability's controller
 * onto the source permanent.
 *
 * @param counterType             the kind of counter to move
 * @param filter                  optional filter for the permanents counters may be removed from
 * @param countersPerMovedCounter number of counters placed on the source for each counter removed
 * @param includeSource           whether the source permanent is eligible as a counter source
 */
public record MoveCountersFromControlledPermanentsToSourceEffect(
        CounterType counterType,
        PermanentPredicate filter,
        int countersPerMovedCounter,
        boolean includeSource
) implements CardEffect {

    public MoveCountersFromControlledPermanentsToSourceEffect(CounterType counterType) {
        this(counterType, null, 1, false);
    }

    public MoveCountersFromControlledPermanentsToSourceEffect(CounterType counterType,
                                                               PermanentPredicate filter,
                                                               int countersPerMovedCounter,
                                                               boolean includeSource) {
        this.counterType = counterType;
        this.filter = filter;
        this.countersPerMovedCounter = countersPerMovedCounter;
        this.includeSource = includeSource;
        if (countersPerMovedCounter < 1) {
            throw new IllegalArgumentException("countersPerMovedCounter must be positive");
        }
    }
}
