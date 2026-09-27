package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Creates a token copy of a permanent whose id was captured by a preceding choice. */
public record CreateTokenCopyOfSelectedPermanentEffect(
        UUID permanentId,
        CreateTokenCopyOfTargetPermanentEffect copyEffect
) implements CardEffect {

    public CreateTokenCopyOfSelectedPermanentEffect(UUID permanentId) {
        this(permanentId, new CreateTokenCopyOfTargetPermanentEffect());
    }
}
