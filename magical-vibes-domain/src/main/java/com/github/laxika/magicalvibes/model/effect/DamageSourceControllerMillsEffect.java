package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature, that source's controller mills
 * that many cards." (Belltower Sphinx.)
 *
 * <p>Registered on the card as a marker (count=0, millingPlayerId=null); the actual values are bound
 * at trigger collection time via {@link #bindDamageSourceController(UUID, int)}.
 */
public record DamageSourceControllerMillsEffect(int count, UUID millingPlayerId)
        implements DamageSourceControllerAwareEffect {

    /** Marker constructor used on card definitions. */
    public DamageSourceControllerMillsEffect() {
        this(0, null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerMillsEffect(damageDealt, controllerId);
    }
}
