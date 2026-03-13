package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage redirect shield that prevents damage from a specific source and redirects it to any target.
 * When the chosen source would deal damage to the protected player or their permanents,
 * the shield prevents up to {@code remainingAmount} damage and deals it to the redirect target instead.
 * Used by Harm's Way.
 *
 * @param protectedPlayerId the player (and their permanents) being protected
 * @param damageSourceId    the permanent chosen as the damage source to prevent from
 * @param remainingAmount   how much prevention remains on this shield
 * @param redirectTargetId  where the redirected damage goes (player UUID or permanent UUID)
 */
public record SourceDamageRedirectShield(
        UUID protectedPlayerId,
        UUID damageSourceId,
        int remainingAmount,
        UUID redirectTargetId
) {
    /**
     * Returns a new shield with the remaining amount reduced by the given consumed amount.
     */
    public SourceDamageRedirectShield withReducedAmount(int consumed) {
        return new SourceDamageRedirectShield(protectedPlayerId, damageSourceId, remainingAmount - consumed, redirectTargetId);
    }
}
