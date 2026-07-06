package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put {@code amount} counter(s) of the specified type on each permanent you control matching the
 * predicate. {@code amount} is a {@link DynamicAmount} ({@link Fixed} for a flat count).
 *
 * <p>For counter placement on permanents outside the controller's battlefield (each attacking
 * creature, each other creature, each creature a target player controls, …) use
 * {@link PutCounterOnEachMatchingPermanentEffect}.</p>
 */
public record PutCounterOnEachControlledPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                        PermanentPredicate predicate) implements CardEffect {

    public PutCounterOnEachControlledPermanentEffect(CounterType counterType, int count,
                                                     PermanentPredicate predicate) {
        this(counterType, new Fixed(count), predicate);
    }
}
