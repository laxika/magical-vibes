package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles target creature(s) and all permanents attached to them that match {@code attachedFilter}.
 * Matching attachments are exiled before the creatures so they are not left on the battlefield
 * when the creatures leave.
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
