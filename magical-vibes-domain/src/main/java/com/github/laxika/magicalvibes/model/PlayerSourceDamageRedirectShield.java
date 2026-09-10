package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A turn-long damage redirect shield protecting only a player from one chosen permanent source.
 */
public record PlayerSourceDamageRedirectShield(
        UUID protectedPlayerId,
        UUID damageSourceId,
        UUID redirectTargetPermanentId
) {
}
