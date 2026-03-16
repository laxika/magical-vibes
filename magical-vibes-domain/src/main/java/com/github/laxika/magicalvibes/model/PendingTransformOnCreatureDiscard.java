package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a pending untap-and-transform action that fires if a creature card is discarded.
 * Set before the discard interaction begins; checked when the player makes their discard choice.
 *
 * @param sourcePermanentId the permanent to untap and transform if a creature is discarded
 */
public record PendingTransformOnCreatureDiscard(UUID sourcePermanentId) {
}
