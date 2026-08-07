package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Matches permanents whose mana value equals the number of counters of the given type on the
 * ability's source permanent ("destroy each creature with mana value equal to the number of age
 * counters on this enchantment" — Wave of Terror).
 *
 * <p>Falls back to {@link FilterContext#sourcePermanentSnapshot()} when the source has already left
 * the battlefield (CR 608.2b last known information).
 *
 * @param counterType the counter type counted on the source permanent
 */
public record PermanentManaValueEqualsSourceCountersPredicate(CounterType counterType)
        implements PermanentPredicate {
}
