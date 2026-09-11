package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put {@code amount} counter(s) of the specified type on each permanent you control matching the
 * predicate. {@code amount} is a {@link DynamicAmount} ({@link Fixed} for a flat count).
 * When {@code excludeTargets} is true, permanents that are declared targets of the spell or
 * ability are skipped.
 *
 * <p>For counter placement on permanents outside the controller's battlefield (each attacking
 * creature, each other creature, each creature a target player controls, …) use
 * {@link PutCounterOnEachMatchingPermanentEffect}.</p>
 */
public record PutCounterOnEachControlledPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                        PermanentPredicate predicate,
                                                        boolean excludeTargets) implements CardEffect {

    public PutCounterOnEachControlledPermanentEffect(CounterType counterType, DynamicAmount amount,
                                                     PermanentPredicate predicate) {
        this(counterType, amount, predicate, false);
    }

    public PutCounterOnEachControlledPermanentEffect(CounterType counterType, int count,
                                                     PermanentPredicate predicate) {
        this(counterType, new Fixed(count), predicate, false);
    }

    /** Convenience for a fixed counter count that skips the spell or ability's declared targets. */
    public PutCounterOnEachControlledPermanentEffect(CounterType counterType, int count,
                                                     PermanentPredicate predicate, boolean excludeTargets) {
        this(counterType, new Fixed(count), predicate, excludeTargets);
    }
}
