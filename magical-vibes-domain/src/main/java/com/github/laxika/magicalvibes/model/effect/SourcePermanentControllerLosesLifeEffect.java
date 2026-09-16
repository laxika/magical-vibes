package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Makes the source permanent's controller lose life. The controller is read from the live source
 * when the effect resolves and is retained here as last-known information if the source left the
 * battlefield after the ability was activated.
 */
public record SourcePermanentControllerLosesLifeEffect(int amount, UUID sourceControllerId)
        implements CardEffect {

    /** Card-definition form; the source controller is bound when the ability is activated. */
    public SourcePermanentControllerLosesLifeEffect(int amount) {
        this(amount, null);
    }
}
