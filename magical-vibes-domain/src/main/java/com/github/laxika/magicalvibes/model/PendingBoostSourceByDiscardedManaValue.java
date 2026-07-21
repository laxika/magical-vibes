package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Tracks a pending "+X/+0 until end of turn" boost that fires when a card is discarded, where
 * X is the discarded card's mana value. Set before the discard interaction begins; checked when
 * the player makes their discard choice.
 *
 * @param sourcePermanentId the permanent to boost by the discarded card's mana value
 */
public record PendingBoostSourceByDiscardedManaValue(UUID sourcePermanentId) {
}
