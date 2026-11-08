package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Exiles every surviving token created by the source permanent: the stack entry's source permanent, which a "when
 * this leaves the battlefield" ability's entry keeps after the source is gone. {@code sourcePermanentId} names it for
 * the engine-built forms (the delayed end-step cleanup this queues, a token-copy handler's own entry); a card leaves
 * it {@code null}. The delayed form queues the cleanup for the beginning of the next end step.
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
