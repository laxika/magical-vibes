package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles target creature and all permanents attached to it that match {@code attachedFilter}.
 * Matching attachments are exiled before the creature so they are not left on the battlefield
 * when the creature leaves.
 *
 * @param attachedFilter predicate over attached permanents
 */
public record ExileTargetAndAttachedMatchingEffect(PermanentPredicate attachedFilter)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
