package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts counters on one matching permanent the resolving spell's controller controls.
 * The choice is made during resolution and is recorded on the stack entry for a following
 * non-targeting effect that applies to the same permanent.
 */
public record PutCounterOnChosenOwnPermanentEffect(CounterType counterType, int count,
                                                    PermanentPredicate predicate,
                                                    boolean recordPlacement) implements CardEffect {

    public PutCounterOnChosenOwnPermanentEffect(CounterType counterType, int count,
                                                PermanentPredicate predicate) {
        this(counterType, count, predicate, false);
    }
}
