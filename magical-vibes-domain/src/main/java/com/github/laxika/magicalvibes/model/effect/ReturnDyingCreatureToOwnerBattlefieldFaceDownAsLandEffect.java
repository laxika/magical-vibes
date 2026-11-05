package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Returns the dying permanent's card, the stack entry's triggering card, face down as a land with the given land
 * subtype.
 */
public record ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect(CardSubtype landSubtype)
        implements CardEffect {

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect() {
        this(CardSubtype.FOREST);
    }

    public ReturnDyingCreatureToOwnerBattlefieldFaceDownAsLandEffect {
        landSubtype = landSubtype == null ? CardSubtype.FOREST : landSubtype;
    }
}
