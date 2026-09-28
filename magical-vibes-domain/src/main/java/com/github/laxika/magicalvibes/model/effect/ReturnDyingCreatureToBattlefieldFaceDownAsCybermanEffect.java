package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Returns the triggering creature card under the ability controller's control as a tapped Cyberman. */
public record ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToBattlefieldFaceDownAsCybermanEffect(dyingCardId);
    }

}
