package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Removes all counters of a specified type from every permanent matching a predicate.
 */
public record RemoveAllCountersFromMatchingPermanentsEffect(CounterType counterType,
                                                             PermanentPredicate predicate)
        implements CardEffect {
}
