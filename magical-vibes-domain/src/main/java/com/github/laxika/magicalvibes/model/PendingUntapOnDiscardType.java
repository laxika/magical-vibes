package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a source permanent that should untap if the current discard interaction discards a card
 * of the required type.
 */
public record PendingUntapOnDiscardType(UUID sourcePermanentId, CardType requiredType) {
}
