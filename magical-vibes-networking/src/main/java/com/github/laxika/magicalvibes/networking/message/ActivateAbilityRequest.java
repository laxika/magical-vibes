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
 */
public record ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments, ManaPaymentIntent paymentIntent) {

    public ActivateAbilityRequest(int permanentIndex, Integer abilityIndex, Integer xValue, UUID targetId, Zone targetZone, List<UUID> targetIds, Map<UUID, Integer> damageAssignments) {
        this(permanentIndex, abilityIndex, xValue, targetId, targetZone, targetIds, damageAssignments, null);
    }
}
