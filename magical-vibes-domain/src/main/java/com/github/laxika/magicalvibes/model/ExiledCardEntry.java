package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a single exiled card with its owner and the permanent that exiled it.
 *
 * @param card              the exiled card
 * @param ownerId           the player whose exile zone this card belongs to
 * @param sourcePermanentId the permanent that caused the exile (for imprint/tracking),
 *                          or {@code null} if not tracked with a specific source
 * @param faceDown          whether the card is exiled face down (CR 406.3 — hideaway,
 *                          Grimoire Thief, Necropotence, ...); hidden from opponents
 */
public record ExiledCardEntry(Card card, UUID ownerId, UUID sourcePermanentId, boolean faceDown) {

    public ExiledCardEntry(Card card, UUID ownerId, UUID sourcePermanentId) {
        this(card, ownerId, sourcePermanentId, false);
    }
}
