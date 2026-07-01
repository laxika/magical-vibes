package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put one or more counters of the specified type on each permanent you control matching the predicate.
 */
public record PutCounterOnEachControlledPermanentEffect(CounterType counterType, int count,
                                                        PermanentPredicate predicate) implements CardEffect {
}
