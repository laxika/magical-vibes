package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the source permanent for each player other than its controller. */
public record CreateTokenCopyOfSourceForEachOtherPlayerEffect(
        CreateTokenCopyOfTargetPermanentEffect copyProfile) implements CardEffect {

    public CreateTokenCopyOfSourceForEachOtherPlayerEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect());
    }
}
