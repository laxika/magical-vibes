package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts {@code count} counters of {@code counterType} on the permanent named by {@code reference} —
 * enchanted/equipped creature, or the permanent that triggered the ability. Never targets and never
 * fizzles; see {@link PermanentReference} for how each value resolves.
 *
 * <p>{@code condition} is optional and checked against the referenced permanent at resolution: if
 * set and the permanent does not match, no counters are placed. This models the M13 Ring cycle's
 * "put a +1/+1 counter on equipped creature if it's blue" upkeep trigger.
 *
 * <p>Whether the counters actually land — {@code cantHaveCounters}, +1/+1 doubling, -1/-1
 * reduction, and the counter-placement triggers — is decided centrally by
 * {@code PermanentCounterSupport.placeCounterOnPermanent}.
 *
 * <p>{@link PermanentReference#SOURCE} is rejected: counters on the ability's own source are owned
 * by {@code PutCountersOnSourceEffect}, which the engine also materialises at runtime for several
 * other effects. Two ways to spell the same placement would split that surface.
 */
public record PutCounterOnReferencedPermanentEffect(
        PermanentReference reference,
        CounterType counterType,
        DynamicAmount count,
        PermanentPredicate condition
) implements CardEffect {

    public PutCounterOnReferencedPermanentEffect {
        if (reference == PermanentReference.SOURCE) {
            throw new IllegalArgumentException(
                    "PermanentReference.SOURCE is not supported here — use PutCountersOnSourceEffect");
        }
    }

    public PutCounterOnReferencedPermanentEffect(PermanentReference reference, CounterType counterType) {
        this(reference, counterType, new Fixed(1), null);
    }

    public PutCounterOnReferencedPermanentEffect(PermanentReference reference, CounterType counterType,
                                                  DynamicAmount count) {
        this(reference, counterType, count, null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType) {
        this(PermanentReference.ATTACHED, counterType, new Fixed(1), null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType, int count) {
        this(PermanentReference.ATTACHED, counterType, new Fixed(count), null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType, DynamicAmount count) {
        this(PermanentReference.ATTACHED, counterType, count, null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType, int count, PermanentPredicate condition) {
        this(PermanentReference.ATTACHED, counterType, new Fixed(count), condition);
    }
}
