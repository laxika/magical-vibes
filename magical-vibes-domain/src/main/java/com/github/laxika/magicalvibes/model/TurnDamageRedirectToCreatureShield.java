package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A turn-scoped damage redirect to a fixed creature. The flags select whether the shield covers only
 * combat damage and whether it also covers damage to permanents controlled by the protected player.
 * Any source and unlimited amount are supported. A shield may optionally match one source permanent
 * instead of every source, and may optionally cover only the protected player rather than their
 * permanents. The redirect only applies while the destination is still a creature on the battlefield,
 * or while it is a creature or planeswalker for Gideon's Sacrifice. Damage dealt to the destination
 * permanent itself is left alone.
 *
 * @param protectedPlayerId             the player whose incoming damage is redirected
 * @param redirectTargetCreatureId      the creature the damage is dealt to instead
 * @param combatOnly                    whether only combat damage is covered
 * @param includeControlledPermanents   whether damage to the protected player's permanents is covered
 * @param damageSourceId                an optional source permanent to match
 * @param allowsPlaneswalker            whether the destination may also be a planeswalker
 */
public record TurnDamageRedirectToCreatureShield(UUID protectedPlayerId,
                                                 UUID redirectTargetCreatureId,
                                                 boolean combatOnly,
                                                 boolean includeControlledPermanents,
                                                 UUID damageSourceId,
                                                 boolean allowsPlaneswalker) {

    public TurnDamageRedirectToCreatureShield(UUID protectedPlayerId, UUID redirectTargetCreatureId,
                                              boolean combatOnly, boolean includeControlledPermanents,
                                              UUID damageSourceId) {
        this(protectedPlayerId, redirectTargetCreatureId, combatOnly, includeControlledPermanents,
                damageSourceId, false);
    }

    public TurnDamageRedirectToCreatureShield(UUID protectedPlayerId, UUID redirectTargetCreatureId,
                                              boolean combatOnly, boolean includeControlledPermanents) {
        this(protectedPlayerId, redirectTargetCreatureId, combatOnly, includeControlledPermanents, null, false);
    }

    public TurnDamageRedirectToCreatureShield(UUID protectedPlayerId, UUID redirectTargetCreatureId) {
        this(protectedPlayerId, redirectTargetCreatureId, false, true, null, false);
    }

    public static TurnDamageRedirectToCreatureShield forCreatureOrPlaneswalker(
            UUID protectedPlayerId, UUID redirectTargetId) {
        return new TurnDamageRedirectToCreatureShield(
                protectedPlayerId, redirectTargetId, false, true, null, true);
    }
}
