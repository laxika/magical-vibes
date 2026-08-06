package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
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
 */
public record PutCounterOnReferencedPermanentEffect(
        PermanentReference reference,
        CounterType counterType,
        int count,
        PermanentPredicate condition
) implements CardEffect {

    public PutCounterOnReferencedPermanentEffect(PermanentReference reference, CounterType counterType) {
        this(reference, counterType, 1, null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType) {
        this(PermanentReference.ATTACHED, counterType, 1, null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType, int count) {
        this(PermanentReference.ATTACHED, counterType, count, null);
    }

    public PutCounterOnReferencedPermanentEffect(CounterType counterType, int count, PermanentPredicate condition) {
        this(PermanentReference.ATTACHED, counterType, count, condition);
    }
}
