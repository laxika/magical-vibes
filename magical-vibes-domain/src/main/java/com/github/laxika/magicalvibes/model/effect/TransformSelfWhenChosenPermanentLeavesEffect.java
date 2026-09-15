package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Transforms the source when the permanent chosen by that source leaves the battlefield, in either
 * direction depending on the source's current face.
 */
public record TransformSelfWhenChosenPermanentLeavesEffect(UUID leavingPermanentId)
        implements CardEffect, LeavingPermanentIdAwareEffect {

    public TransformSelfWhenChosenPermanentLeavesEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToLeavingPermanentId(UUID permanentId) {
        return new TransformSelfWhenChosenPermanentLeavesEffect(permanentId);
    }
}
