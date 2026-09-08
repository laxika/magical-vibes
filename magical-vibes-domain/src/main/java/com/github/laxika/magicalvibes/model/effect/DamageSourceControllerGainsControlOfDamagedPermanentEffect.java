package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature, that source's controller
 * gains control of this creature."
 *
 * <p>The card-level no-argument form is a marker. The damage source's controller is bound when
 * the damage trigger is collected, before the triggered ability is put on the stack.
 */
public record DamageSourceControllerGainsControlOfDamagedPermanentEffect(UUID damageSourceControllerId)
        implements DamageSourceControllerAwareEffect {

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerGainsControlOfDamagedPermanentEffect() {
        this(null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerGainsControlOfDamagedPermanentEffect(controllerId);
    }
}
