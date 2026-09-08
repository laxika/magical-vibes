package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Transforms the source when the permanent chosen by that source leaves the battlefield.
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
