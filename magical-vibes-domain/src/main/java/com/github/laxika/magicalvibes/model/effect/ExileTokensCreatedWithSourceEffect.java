package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles every surviving token created by the source permanent. The source id is supplied by the
 * leaves-the-battlefield trigger collector because the source is already gone when the effect
 * resolves. The delayed form queues the cleanup for the beginning of the next end step.
 */
public record ExileTokensCreatedWithSourceEffect(UUID sourcePermanentId, boolean atNextEndStep)
        implements CardEffect {

    public ExileTokensCreatedWithSourceEffect() {
        this(null, false);
    }

    public ExileTokensCreatedWithSourceEffect(UUID sourcePermanentId) {
        this(sourcePermanentId, false);
    }

    public ExileTokensCreatedWithSourceEffect(boolean atNextEndStep) {
        this(null, atNextEndStep);
    }
}
