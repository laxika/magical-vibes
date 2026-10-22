package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Makes the source permanent's controller lose life. The controller is read from the live source
 * when the effect resolves; if the source has left the battlefield, the controller it had immediately
 * before leaving is used as last-known information, falling back to the controller bound at activation.
 */
public record SourcePermanentControllerLosesLifeEffect(int amount, UUID sourceControllerId)
        implements CardEffect {

    /** Card-definition form; the source controller is bound when the ability is activated. */
    public SourcePermanentControllerLosesLifeEffect(int amount) {
        this(amount, null);
    }
}
