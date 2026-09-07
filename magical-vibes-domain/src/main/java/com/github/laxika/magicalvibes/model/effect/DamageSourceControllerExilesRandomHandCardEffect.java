package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect for exiling a random card from the damage source controller's hand and offering
 * the exiled card for a land play or a free cast.
 *
 * <p>The source controller is bound when the damage event is collected because it is not known when
 * the card is defined.
 */
public record DamageSourceControllerExilesRandomHandCardEffect(UUID sourceControllerId)
        implements DamageSourceControllerAwareEffect {

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerExilesRandomHandCardEffect() {
        this(null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) {
            return this;
        }
        return new DamageSourceControllerExilesRandomHandCardEffect(controllerId);
    }
}
