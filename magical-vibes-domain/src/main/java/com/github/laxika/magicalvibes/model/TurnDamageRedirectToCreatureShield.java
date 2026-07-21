package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Saving Grace: for the rest of the turn, all damage that would be dealt to {@code protectedPlayerId}
 * or a permanent they control is dealt to {@code redirectTargetCreatureId} instead. Any source,
 * unlimited amount. The redirect only applies while the destination is still a creature on the
 * battlefield (ruling: if it isn't, the damage isn't redirected). Damage dealt to the destination
 * creature itself is left alone (redirecting to itself is a no-op).
 *
 * @param protectedPlayerId        the player whose (and whose permanents') incoming damage is redirected
 * @param redirectTargetCreatureId the creature the damage is dealt to instead
 */
public record TurnDamageRedirectToCreatureShield(UUID protectedPlayerId, UUID redirectTargetCreatureId) {
}
