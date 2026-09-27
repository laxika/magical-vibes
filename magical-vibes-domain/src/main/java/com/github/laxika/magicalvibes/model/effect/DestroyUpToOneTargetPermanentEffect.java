package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Destroys zero or one targeted permanent, as chosen when the ability is put on the stack. */
public record DestroyUpToOneTargetPermanentEffect(PermanentPredicate targetFilter)
        implements RemovalEffect, OptionalTargetEffect {

    public DestroyUpToOneTargetPermanentEffect() {
        this(null);
    }

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
