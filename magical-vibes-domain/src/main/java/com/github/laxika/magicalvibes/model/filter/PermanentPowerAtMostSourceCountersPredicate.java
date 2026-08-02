package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches creatures whose effective power is at most the number of counters of the given type on the
 * ability's source permanent ("target creature with power less than or equal to the number of treasure
 * counters on this enchantment" — Legacy's Allure).
 *
 * <p>The source is typically sacrificed to pay the ability's cost, so once it has left the battlefield
 * the count comes from {@link FilterContext#sourcePermanentSnapshot()} (CR 608.2b last known information).
 *
 * @param counterType the counter type counted on the source permanent
 */
public record PermanentPowerAtMostSourceCountersPredicate(CounterType counterType) implements PermanentPredicate {
}
