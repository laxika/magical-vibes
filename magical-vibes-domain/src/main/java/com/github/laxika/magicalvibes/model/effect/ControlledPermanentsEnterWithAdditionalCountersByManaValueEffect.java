package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that gives matching permanents additional +1/+1 counters as they enter,
 * choosing the amount from the entering permanent's mana value.
 */
public record ControlledPermanentsEnterWithAdditionalCountersByManaValueEffect(
        PermanentPredicate enteringPermanentPredicate,
        int maximumManaValue,
        int atMostMaximumManaValueCount,
        int aboveMaximumManaValueCount
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public int additionalCounterCount(Permanent enteringPermanent) {
        return enteringPermanent.getCard().getManaValue() <= maximumManaValue
                ? atMostMaximumManaValueCount
                : aboveMaximumManaValueCount;
    }
}
