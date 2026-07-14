package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage redirect shield that redirects damage dealt to a specific creature this turn onto another
 * permanent instead. Two flavours are supported:
 * <ul>
 *   <li><b>Unlimited, chosen source</b> ({@link #UNLIMITED} amount, non-null {@code damageSourceId}):
 *       every point of damage the chosen source would deal to the protected creature this turn is
 *       redirected. Used by Oracle's Attendants.</li>
 *   <li><b>Amount-limited, any source</b> (positive {@code remainingAmount}, null {@code damageSourceId}):
 *       only the next {@code remainingAmount} damage from any source is redirected, then the shield is
 *       consumed. Used by Zealous Inquisitor.</li>
 *   <li><b>Next-event, chosen source</b> ({@link #NEXT_EVENT} amount, non-null {@code damageSourceId}):
 *       the next single damage event the chosen source would deal to the protected creature this turn is
 *       redirected in full, then the shield is consumed. Used by Jade Monolith.</li>
 * </ul>
 * Unlike {@link SourceDamageRedirectShield} (Harm's Way) this protects a single creature (not a player
 * and their permanents).
 *
 * @param protectedPermanentId the creature whose incoming damage is redirected
 * @param damageSourceId       the permanent chosen as the damage source to redirect from, or {@code null}
 *                             to match any source
 * @param remainingAmount      how much damage remains to redirect, or {@link #UNLIMITED} for no limit
 * @param redirectTargetId     where the redirected damage goes (typically the shield's own creature)
 */
public record CreatureDamageRedirectShield(
        UUID protectedPermanentId,
        UUID damageSourceId,
        int remainingAmount,
        UUID redirectTargetId
) {
    /** Sentinel {@code remainingAmount} meaning the shield has no amount limit. */
    public static final int UNLIMITED = -1;

    /** Sentinel {@code remainingAmount} meaning the shield redirects only the next single damage event, in full. */
    public static final int NEXT_EVENT = -2;

    public boolean isUnlimited() {
        return remainingAmount == UNLIMITED;
    }

    public boolean isNextEvent() {
        return remainingAmount == NEXT_EVENT;
    }

    /**
     * Returns a new shield with the remaining amount reduced by the given consumed amount.
     * Only meaningful for amount-limited shields.
     */
    public CreatureDamageRedirectShield withReducedAmount(int consumed) {
        return new CreatureDamageRedirectShield(protectedPermanentId, damageSourceId, remainingAmount - consumed, redirectTargetId);
    }
}
