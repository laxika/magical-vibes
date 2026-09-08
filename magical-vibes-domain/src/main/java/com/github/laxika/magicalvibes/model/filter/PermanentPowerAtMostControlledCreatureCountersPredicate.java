package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches permanents whose effective power is at most the number of the specified counters on
 * creatures controlled by the source's controller.
 */
public record PermanentPowerAtMostControlledCreatureCountersPredicate(CounterType counterType)
        implements PermanentPredicate {
}
