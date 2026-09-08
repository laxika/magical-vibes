package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Destroys a target noncreature permanent, then its controller may copy the resolving spell.
 */
public record DestroyTargetPermanentThenMayCopyEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        );
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
