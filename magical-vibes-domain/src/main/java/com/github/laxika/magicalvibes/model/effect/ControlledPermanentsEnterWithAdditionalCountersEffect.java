package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that gives matching permanents controlled by the source's controller
 * additional counters as they enter the battlefield. The two-argument constructor defaults to
 * +1/+1 counters.
 */
public record ControlledPermanentsEnterWithAdditionalCountersEffect(
        PermanentPredicate enteringPermanentPredicate,
        CounterType counterType,
        int count
) implements ControlledPermanentEntryReplacementEffect {

    public ControlledPermanentsEnterWithAdditionalCountersEffect(
            PermanentPredicate enteringPermanentPredicate, int count) {
        this(enteringPermanentPredicate, CounterType.PLUS_ONE_PLUS_ONE, count);
    }

    @Override
    public int additionalCounterCount(Permanent enteringPermanent) {
        return count;
    }
}
