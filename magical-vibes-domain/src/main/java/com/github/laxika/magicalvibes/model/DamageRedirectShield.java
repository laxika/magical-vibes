package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage prevention shield whose prevented damage is dealt on to another target.
 * When damage that would be dealt to {@code protectedPlayerId}, or to the exact
 * {@code protectedPermanentId} when present, is prevented by this shield, {@code sourceCard}
 * deals that much damage to {@code redirectTargetId}.
 *
 * @param protectedPlayerId          the player associated with the shield
 * @param remainingAmount            how much prevention remains on this shield
 * @param sourcePermanentId          the permanent that deals the damage, when there is one
 * @param sourceCard                 the card for logging and damage source context
 * @param redirectTargetId           the target that receives the damage
 * @param coversControlledPermanents whether the shield also covers permanents the protected
 *                                   player controls
 * @param protectedPermanentId       an exact protected permanent, or {@code null} for a player
 *                                  shield
 */
public record DamageRedirectShield(
        UUID protectedPlayerId,
        int remainingAmount,
        UUID sourcePermanentId,
        Card sourceCard,
        UUID redirectTargetId,
        boolean coversControlledPermanents,
        UUID protectedPermanentId
) {
    /** Player-only shield (Vengeful Archon). */
    public DamageRedirectShield(UUID protectedPlayerId, int remainingAmount, UUID sourcePermanentId,
                                Card sourceCard, UUID redirectTargetId) {
        this(protectedPlayerId, remainingAmount, sourcePermanentId, sourceCard, redirectTargetId, false, null);
    }

    /** Player-and-controlled-permanents shield (Divine Deflection). */
    public DamageRedirectShield(UUID protectedPlayerId, int remainingAmount, UUID sourcePermanentId,
                                Card sourceCard, UUID redirectTargetId, boolean coversControlledPermanents) {
        this(protectedPlayerId, remainingAmount, sourcePermanentId, sourceCard, redirectTargetId,
                coversControlledPermanents, null);
    }

    /** Returns a new shield with the remaining amount reduced by the given consumed amount. */
    public DamageRedirectShield withReducedAmount(int consumed) {
        return new DamageRedirectShield(protectedPlayerId, remainingAmount - consumed, sourcePermanentId,
                sourceCard, redirectTargetId, coversControlledPermanents, protectedPermanentId);
    }
}
