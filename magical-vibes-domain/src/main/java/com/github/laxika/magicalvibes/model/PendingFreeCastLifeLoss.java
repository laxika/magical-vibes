package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * A free-cast offer whose spell, if actually cast, makes its owner lose life equal to its mana value
 * once the casts are finished (Kefka, Dancing Mad). Applied when the free-cast queue drains, only
 * for cards that left exile because they were cast.
 */
public record PendingFreeCastLifeLoss(UUID cardId, UUID ownerId, int manaValue, String sourceName) {
}
