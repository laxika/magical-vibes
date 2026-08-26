package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that gives matching permanents controlled by the source's controller
 * additional +1/+1 counters as they enter the battlefield.
 */
public record ControlledPermanentsEnterWithAdditionalCountersEffect(
        PermanentPredicate enteringPermanentPredicate,
        int count
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public int additionalCounterCount(Permanent enteringPermanent) {
        return count;
    }
}
