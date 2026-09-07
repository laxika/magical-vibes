package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The sum of a counter type on battlefield permanents matching a predicate within a scope.
 */
public record PermanentCounterSum(CounterType counterType, PermanentPredicate filter,
                                  CountScope scope) implements DynamicAmount {
}
