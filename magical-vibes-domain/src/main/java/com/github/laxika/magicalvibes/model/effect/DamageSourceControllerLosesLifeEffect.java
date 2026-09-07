package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: the controller of the source that dealt damage loses that much life.
 *
 * <p>The no-argument form is a card-definition marker. The damage amount and source controller
 * are bound when the damage trigger is collected, before the triggered ability is put on the stack.
 */
public record DamageSourceControllerLosesLifeEffect(int amount, UUID sourceControllerId)
        implements DamageSourceControllerAwareEffect {

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerLosesLifeEffect() {
        this(0, null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerLosesLifeEffect(damageDealt, controllerId);
    }
}
