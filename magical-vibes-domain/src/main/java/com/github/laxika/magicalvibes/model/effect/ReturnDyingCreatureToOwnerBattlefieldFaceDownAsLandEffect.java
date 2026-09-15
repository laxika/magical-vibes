package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.UUID;

/** Returns a bound dying permanent card face down as a land with the given land subtype. */
public record ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect(
        UUID dyingCardId,
        CardSubtype landSubtype
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect() {
        this(null, CardSubtype.FOREST);
    }

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect(CardSubtype landSubtype) {
        this(null, landSubtype);
    }

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect {
        landSubtype = landSubtype == null ? CardSubtype.FOREST : landSubtype;
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect(dyingCardId, landSubtype);
    }
}
