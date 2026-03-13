package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage prevention shield that redirects prevented damage to a target player.
 * When damage that would be dealt to {@code protectedPlayerId} is prevented by this shield,
 * the source permanent deals that much damage to {@code redirectTargetPlayerId}.
 *
 * @param protectedPlayerId    the player whose damage is being prevented
 * @param remainingAmount      how much prevention remains on this shield
 * @param sourcePermanentId    the permanent that deals the redirected damage (e.g. Vengeful Archon)
 * @param sourceCard           the card for logging and damage source context
 * @param redirectTargetPlayerId the player who receives the redirected damage
 */
public record DamageRedirectShield(
        UUID protectedPlayerId,
        int remainingAmount,
        UUID sourcePermanentId,
        Card sourceCard,
        UUID redirectTargetPlayerId
) {
    /**
     * Returns a new shield with the remaining amount reduced by the given consumed amount.
     */
    public DamageRedirectShield withReducedAmount(int consumed) {
        return new DamageRedirectShield(protectedPlayerId, remainingAmount - consumed, sourcePermanentId, sourceCard, redirectTargetPlayerId);
    }
}
