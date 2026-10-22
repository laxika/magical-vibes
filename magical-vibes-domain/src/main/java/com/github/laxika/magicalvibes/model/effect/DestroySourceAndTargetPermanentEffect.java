package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Destroys the source permanent and the targeted permanent as one simultaneous event, so each
 * dying permanent's leaves-the-battlefield abilities see the other die with it (Wall of Vipers:
 * "Destroy this creature and target creature it's blocking").
 *
 * @param targetFilter predicate narrowing the permanent target
 */
public record DestroySourceAndTargetPermanentEffect(PermanentPredicate targetFilter) implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
