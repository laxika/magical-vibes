package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: the controller of the source that dealt damage loses that much life.
 *
 * <p>The no-argument form is a card-definition marker. The damage amount and source controller
 * are bound when the damage trigger is collected, before the triggered ability is put on the stack.
 * When {@code currentControllerAtResolution} is set (Visions of Brutality: "its controller loses that
 * much life"), the controller of the trigger's source permanent is read again on resolution, per CR
 * 608.2h, and the bound controller is used only if that permanent has left the battlefield.
 */
public record DamageSourceControllerLosesLifeEffect(int amount, UUID sourceControllerId,
                                                    boolean currentControllerAtResolution)
        implements DamageSourceControllerAwareEffect {

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerLosesLifeEffect() {
        this(0, null, false);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerLosesLifeEffect(damageDealt, controllerId, false);
    }

    /** Binds the damage amount and the trigger-time controller, deferring to the resolution-time controller. */
    public DamageSourceControllerLosesLifeEffect bindCurrentControllerAtResolution(UUID controllerId,
                                                                                  int damageDealt) {
        if (damageDealt <= 0) return this;
        return new DamageSourceControllerLosesLifeEffect(damageDealt, controllerId, true);
    }
}
