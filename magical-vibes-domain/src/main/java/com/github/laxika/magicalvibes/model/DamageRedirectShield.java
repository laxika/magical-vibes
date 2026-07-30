package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage prevention shield whose prevented damage is dealt on to another target.
 * When damage that would be dealt to {@code protectedPlayerId} — or, when
 * {@code coversControlledPermanents} is set, to a permanent that player controls — is prevented
 * by this shield, {@code sourceCard} deals that much damage to {@code redirectTargetId}.
 *
 * @param protectedPlayerId          the player whose damage is being prevented
 * @param remainingAmount            how much prevention remains on this shield
 * @param sourcePermanentId          the permanent that deals the damage, when the source is one
 *                                   (e.g. Vengeful Archon); {@code null} for a spell source
 * @param sourceCard                 the card for logging and damage source context
 * @param redirectTargetId           the target that receives the damage — a player, planeswalker
 *                                   or creature
 * @param coversControlledPermanents whether the shield also covers permanents the protected
 *                                   player controls (Divine Deflection), not just the player
 */
public record DamageRedirectShield(
        UUID protectedPlayerId,
        int remainingAmount,
        UUID sourcePermanentId,
        Card sourceCard,
        UUID redirectTargetId,
        boolean coversControlledPermanents
) {
    /** Player-only shield (Vengeful Archon): permanents the player controls are not covered. */
    public DamageRedirectShield(UUID protectedPlayerId, int remainingAmount, UUID sourcePermanentId,
                                Card sourceCard, UUID redirectTargetId) {
        this(protectedPlayerId, remainingAmount, sourcePermanentId, sourceCard, redirectTargetId, false);
    }

    /**
     * Returns a new shield with the remaining amount reduced by the given consumed amount.
     */
    public DamageRedirectShield withReducedAmount(int consumed) {
        return new DamageRedirectShield(protectedPlayerId, remainingAmount - consumed, sourcePermanentId,
                sourceCard, redirectTargetId, coversControlledPermanents);
    }
}
