package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A damage redirect shield that redirects ALL damage a chosen source would deal to a specific
 * creature this turn onto another permanent instead. Unlike {@link SourceDamageRedirectShield}
 * (Harm's Way), this protects a single creature (not a player and their permanents) and has no
 * amount limit — every point of damage from the chosen source to the protected creature this turn
 * is redirected. Used by Oracle's Attendants.
 *
 * @param protectedPermanentId the creature whose incoming damage from the chosen source is redirected
 * @param damageSourceId       the permanent chosen as the damage source to redirect from
 * @param redirectTargetId     where the redirected damage goes (typically the shield's own creature)
 */
public record CreatureDamageRedirectShield(
        UUID protectedPermanentId,
        UUID damageSourceId,
        UUID redirectTargetId
) {
}
