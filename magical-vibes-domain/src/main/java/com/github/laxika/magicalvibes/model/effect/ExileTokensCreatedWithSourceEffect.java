package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles every surviving token created by the source permanent. The source id is supplied by the
 * leaves-the-battlefield trigger collector because the source is already gone when the effect
 * resolves.
 */
public record ExileTokensCreatedWithSourceEffect(UUID sourcePermanentId) implements CardEffect {

    public ExileTokensCreatedWithSourceEffect() {
        this(null);
    }
}
