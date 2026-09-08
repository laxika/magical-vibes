package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of a creature card exiled with the source permanent. */
public record CreateTokenCopyOfExiledCreatureWithSourceEffect(
        CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect
) implements CardEffect {
}
