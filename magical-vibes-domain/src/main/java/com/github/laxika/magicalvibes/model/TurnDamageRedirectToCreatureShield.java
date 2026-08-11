package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A turn-scoped damage redirect to a fixed creature. The flags select whether the shield covers only
 * combat damage and whether it also covers damage to permanents controlled by the protected player.
 * Any source and unlimited amount are supported. The redirect only applies while the destination is
 * still a creature on the battlefield. Damage dealt to the destination creature itself is left alone.
 *
 * @param protectedPlayerId             the player whose incoming damage is redirected
 * @param redirectTargetCreatureId      the creature the damage is dealt to instead
 * @param combatOnly                    whether only combat damage is covered
 * @param includeControlledPermanents   whether damage to the protected player's permanents is covered
 */
public record TurnDamageRedirectToCreatureShield(UUID protectedPlayerId,
                                                 UUID redirectTargetCreatureId,
                                                 boolean combatOnly,
                                                 boolean includeControlledPermanents) {

    public TurnDamageRedirectToCreatureShield(UUID protectedPlayerId, UUID redirectTargetCreatureId) {
        this(protectedPlayerId, redirectTargetCreatureId, false, true);
    }
}
