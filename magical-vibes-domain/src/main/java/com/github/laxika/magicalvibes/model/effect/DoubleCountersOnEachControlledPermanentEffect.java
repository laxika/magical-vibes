package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Doubles every kind of counter on each controlled permanent matching the predicate. */
public record DoubleCountersOnEachControlledPermanentEffect(PermanentPredicate predicate)
        implements CardEffect {
}
