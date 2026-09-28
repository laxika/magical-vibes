package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys any number of target permanents matching {@code filter}, provided their combined mana
 * value is at most the evaluated limit.
 */
public record DestroyTargetPermanentsWithinTotalManaValueEffect(
        PermanentPredicate filter,
        DynamicAmount totalManaValueLimit
) implements AggregateManaValueTargetEffect, RemovalEffect {

    public DestroyTargetPermanentsWithinTotalManaValueEffect {
        if (filter == null) {
            throw new IllegalArgumentException("filter cannot be null");
        }
        if (totalManaValueLimit == null) {
            throw new IllegalArgumentException("totalManaValueLimit cannot be null");
        }
    }

    public static DestroyTargetPermanentsWithinTotalManaValueEffect withinTotalManaValue(
            PermanentPredicate filter, DynamicAmount totalManaValueLimit) {
        return new DestroyTargetPermanentsWithinTotalManaValueEffect(filter, totalManaValueLimit);
    }

    @Override
    public int maxTotalManaValue() {
        return Integer.MAX_VALUE;
    }

    @Override
    public DynamicAmount dynamicMaxTotalManaValue() {
        return totalManaValueLimit;
    }

    @Override
    public boolean hasAggregateManaValueLimit() {
        return true;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), filter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
