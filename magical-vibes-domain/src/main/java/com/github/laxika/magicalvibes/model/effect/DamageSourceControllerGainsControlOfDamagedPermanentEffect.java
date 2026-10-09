package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature, that source's controller
 * gains control of this creature."
 *
 * <p>The card-level no-argument form is a marker. The damage source's controller is bound when
 * the damage trigger is collected. A permanent damage source is also retained so its current
 * controller can be determined when the ability resolves; a departed source uses the captured
 * controller.
 */
public record DamageSourceControllerGainsControlOfDamagedPermanentEffect(UUID damageSourceControllerId,
                                                                         UUID damageSourcePermanentId)
        implements DamageSourceControllerAwareEffect {

    public DamageSourceControllerGainsControlOfDamagedPermanentEffect(UUID damageSourceControllerId) {
        this(damageSourceControllerId, null);
    }

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerGainsControlOfDamagedPermanentEffect() {
        this(null, null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerGainsControlOfDamagedPermanentEffect(controllerId);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt, UUID sourcePermanentId) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerGainsControlOfDamagedPermanentEffect(controllerId, sourcePermanentId);
    }
}
