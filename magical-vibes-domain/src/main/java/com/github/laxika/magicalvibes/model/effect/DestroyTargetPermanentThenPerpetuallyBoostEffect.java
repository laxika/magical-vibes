package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Destroys the target permanent and records a perpetual power/toughness boost for its card. */
public record DestroyTargetPermanentThenPerpetuallyBoostEffect(
        int powerBoost,
        int toughnessBoost,
        PermanentPredicate targetFilter
) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
