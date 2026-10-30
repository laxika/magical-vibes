package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Triggered effect: "Whenever a source deals damage to this creature,
 * that source's controller sacrifices that many permanents."
 *
 * <p>Registered on the card as a marker (count=0, sacrificingPlayerId=null).
 * At trigger collection time, a new instance with actual values is created
 * and placed on the stack. A permanent damage source is also retained so its current controller
 * can be determined when the ability resolves; a departed source uses the captured controller.
 */
public record DamageSourceControllerSacrificesPermanentsEffect(int count, UUID sacrificingPlayerId,
                                                               UUID damageSourcePermanentId)
        implements DamageSourceControllerAwareEffect {

    public DamageSourceControllerSacrificesPermanentsEffect(int count, UUID sacrificingPlayerId) {
        this(count, sacrificingPlayerId, null);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerSacrificesPermanentsEffect(damageDealt, controllerId);
    }

    @Override
    public CardEffect bindDamageSourceController(UUID controllerId, int damageDealt, UUID sourcePermanentId) {
        if (controllerId == null || damageDealt <= 0) return this;
        return new DamageSourceControllerSacrificesPermanentsEffect(damageDealt, controllerId, sourcePermanentId);
    }
}
