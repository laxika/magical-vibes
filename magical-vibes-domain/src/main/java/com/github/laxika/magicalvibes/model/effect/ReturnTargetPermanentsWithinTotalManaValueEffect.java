package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Returns any number of target permanents to their owners' hands when their combined mana value
 * is at most the configured limit.
 */
public record ReturnTargetPermanentsWithinTotalManaValueEffect(
        PermanentPredicate filter,
        int maxTotalManaValue
) implements AggregateManaValueTargetEffect {

    public ReturnTargetPermanentsWithinTotalManaValueEffect {
        if (maxTotalManaValue < 0) {
            throw new IllegalArgumentException("maxTotalManaValue cannot be negative");
        }
    }

    public static ReturnTargetPermanentsWithinTotalManaValueEffect withinTotalManaValue(
            PermanentPredicate filter, int maxTotalManaValue) {
        return new ReturnTargetPermanentsWithinTotalManaValueEffect(filter, maxTotalManaValue);
    }

    @Override
    public boolean hasAggregateManaValueLimit() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), filter);
    }
}
