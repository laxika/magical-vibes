package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.ManaPaymentIntent;
import com.github.laxika.magicalvibes.model.Zone;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @param paymentIntent what the player is tapping this mana source for, when the activation is a
 *                      mana ability serving a held-back cast or activation; {@code null} otherwise.
 *                      Advisory only — see {@link ManaPaymentIntent}.
 * @param sourcePermanentId optional ID of the permanent owning the ability. When present it is used instead of
 *                          {@code permanentIndex}, so an "any player may activate" ability on another player's
 *                          permanent is not shadowed by the activator's own permanent at the same index.
 */
public record ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent, UUID sourcePermanentId) {

    public ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent) {
        this(permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments, paymentIntent, null);
    }

    public ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        this(permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments, null);
    }
}
