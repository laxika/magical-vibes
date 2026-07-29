package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts the targeted creature on top of its owner's library. If the creature matches
 * {@code bottomOptionCondition}, the spell's controller may instead put it on the bottom of its
 * owner's library — the two destinations are exclusive, so declining leaves it on top. Used by
 * Ether Well ("Put target creature on top of its owner's library. If that creature is red, you may
 * put it on the bottom of its owner's library instead.").
 *
 * @param bottomOptionCondition when the target matches, the controller is offered the
 *                              bottom-of-library destination instead of the top
 */
public record PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect(
        PermanentPredicate bottomOptionCondition) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
