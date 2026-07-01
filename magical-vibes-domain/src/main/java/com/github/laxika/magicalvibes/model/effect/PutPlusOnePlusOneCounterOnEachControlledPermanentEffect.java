package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put a +1/+1 counter on each permanent you control matching the predicate.
 */
public record PutPlusOnePlusOneCounterOnEachControlledPermanentEffect(PermanentPredicate predicate)
        implements CardEffect {
}
