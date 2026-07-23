package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Return target permanent and all permanents attached to it that match {@code attachedFilter}
 * to their owners' hands. Matching attachments are bounced before the target so they are not
 * orphaned into the graveyard (Word of Undoing: white Auras you own attached to the creature).
 *
 * @param attachedFilter predicate over attached permanents (Aura + color + ownership, etc.)
 */
public record ReturnTargetAndAttachedMatchingToHandEffect(PermanentPredicate attachedFilter)
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.BOUNCE;
    }
}
