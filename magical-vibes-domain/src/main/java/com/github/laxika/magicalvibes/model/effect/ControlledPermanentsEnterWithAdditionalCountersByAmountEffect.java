package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect that gives matching permanents additional +1/+1 counters as they
 * enter, with the number evaluated from the source's current game state.
 */
public record ControlledPermanentsEnterWithAdditionalCountersByAmountEffect(
        PermanentPredicate enteringPermanentPredicate,
        DynamicAmount count
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public int additionalCounterCount(Permanent enteringPermanent) {
        return 0;
    }

    @Override
    public DynamicAmount additionalCounterAmount() {
        return count;
    }
}
