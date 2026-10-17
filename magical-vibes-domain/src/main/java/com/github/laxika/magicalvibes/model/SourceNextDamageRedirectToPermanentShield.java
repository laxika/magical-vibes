package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Opal-Eye, Konda's Yojimbo: the next damage event the chosen source would deal this turn — to any
 * recipient — is dealt to {@code destinationPermanentId} instead. One-shot: the shield is consumed by
 * the first matching damage event. The redirect only applies while the destination is still a creature
 * on the battlefield. Damage dealt to the destination itself is dealt normally but still uses the
 * shield up. Damage the same source deals at the same time to other recipients is redirected too.
 *
 * @param sourceId                the permanent chosen as the damage source
 * @param destinationPermanentId  the permanent the damage is dealt to instead
 */
public record SourceNextDamageRedirectToPermanentShield(UUID sourceId, UUID destinationPermanentId) {
}
