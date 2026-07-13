package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Remove up to {@code amount} counter(s) of the specified type from each permanent you control
 * matching the predicate. A permanent with fewer than {@code amount} counters of that type simply
 * has all of them removed (the count is clamped at zero); a permanent with none is unaffected.
 *
 * <p>The mirror of {@link PutCounterOnEachControlledPermanentEffect}. Used by Heartmender
 * ("remove a -1/-1 counter from each creature you control").</p>
 */
public record RemoveCounterFromEachControlledPermanentEffect(CounterType counterType, int amount,
                                                             PermanentPredicate predicate) implements CardEffect {
}
