package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Transforms the permanent that granted this temporary effect. */
public record TransformGrantingPermanentEffect(UUID grantingPermanentId)
        implements CardEffect, GrantingPermanentAwareEffect {

    public TransformGrantingPermanentEffect() {
        this(null);
    }

    @Override
    public CardEffect withGrantingPermanentId(UUID permanentId) {
        return new TransformGrantingPermanentEffect(permanentId);
    }
}
