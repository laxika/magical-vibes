package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Destroy target nonland permanent and all other permanents with the same name as that permanent.
 * Destruction respects regeneration / indestructible. Used by Maelstrom Pulse.
 */
public record DestroyTargetNonlandPermanentAndAllWithSameNameEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT,
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
