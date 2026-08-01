package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches permanents whose mana value is less than or equal to the number of counters of
 * {@code counterType} on that permanent. Used by Corrosion ("destroy each artifact with mana value
 * less than or equal to the number of rust counters on it").
 */
public record PermanentManaValueAtMostOwnCountersPredicate(CounterType counterType)
        implements PermanentPredicate {
}
