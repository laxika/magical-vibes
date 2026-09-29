package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/** Exiles each chosen nonland permanent, then its controller cloaks the top card of their library. */
public record ExileTargetPermanentsAndControllersCloakEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
