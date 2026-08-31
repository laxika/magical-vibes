package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * One-shot redirection shield covering a permanent and its controller. The first damage event
 * dealt to either protected recipient is redirected to the stored target.
 *
 * @param protectedPermanentId the permanent whose incoming damage is protected, or {@code null}
 *                             when it has already left the battlefield
 * @param protectedPlayerId the controller whose incoming damage is protected
 * @param redirectTargetId where the redirected damage is dealt instead
 */
public record SourcePermanentAndControllerNextDamageRedirectShield(
        UUID protectedPermanentId,
        UUID protectedPlayerId,
        UUID redirectTargetId
) {
}
