package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * An amount-limited, any-source redirect shield protecting a <em>player</em>: the next
 * {@code remainingAmount} damage that would be dealt to {@code protectedPlayerId} this turn is dealt
 * to {@code redirectTargetPermanentId} instead, then the shield is consumed. Used by Martyrdom.
 *
 * <p>Unlike {@link SourceDamageRedirectShield} (Harm's Way) this protects only the player, not the
 * permanents they control, and it matches any source rather than one chosen source. Unlike
 * {@link TurnDamageRedirectToCreatureShield} (Saving Grace) it is amount-limited rather than
 * lasting the whole turn. It is the player-protecting counterpart of the amount-limited flavour of
 * {@link CreatureDamageRedirectShield}.</p>
 *
 * @param protectedPlayerId         the player whose incoming damage is redirected
 * @param remainingAmount           how much damage remains to redirect
 * @param redirectTargetPermanentId the permanent the redirected damage is dealt to instead
 */
public record PlayerNextDamageRedirectShield(
        UUID protectedPlayerId,
        int remainingAmount,
        UUID redirectTargetPermanentId
) {
    /** Returns a new shield with the remaining amount reduced by the given consumed amount. */
    public PlayerNextDamageRedirectShield withReducedAmount(int consumed) {
        return new PlayerNextDamageRedirectShield(protectedPlayerId, remainingAmount - consumed, redirectTargetPermanentId);
    }
}
