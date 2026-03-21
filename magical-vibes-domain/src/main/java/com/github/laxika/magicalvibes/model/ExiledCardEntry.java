package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a single exiled card with its owner and the permanent that exiled it.
 *
 * @param card              the exiled card
 * @param ownerId           the player whose exile zone this card belongs to
 * @param sourcePermanentId the permanent that caused the exile (for imprint/tracking),
 *                          or {@code null} if not tracked with a specific source
 */
public record ExiledCardEntry(Card card, UUID ownerId, UUID sourcePermanentId) {
}
