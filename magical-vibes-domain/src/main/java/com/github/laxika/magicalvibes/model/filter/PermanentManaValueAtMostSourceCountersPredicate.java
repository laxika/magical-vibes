package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches permanents whose mana value is at most the number of counters of the given type on the
 * evaluating source permanent.
 */
public record PermanentManaValueAtMostSourceCountersPredicate(CounterType counterType)
        implements PermanentPredicate {
}
