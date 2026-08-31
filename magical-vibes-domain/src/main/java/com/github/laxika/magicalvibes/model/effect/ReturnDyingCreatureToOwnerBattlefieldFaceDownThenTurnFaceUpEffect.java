package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns a bound dying permanent card to its owner's battlefield face down, then turns it face up
 * when the card's front face is a creature and turning it face up is allowed.
 */
public record ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect(UUID dyingCardId)
        implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToOwnerBattlefieldFaceDownThenTurnFaceUpEffect(dyingCardId);
    }
}
