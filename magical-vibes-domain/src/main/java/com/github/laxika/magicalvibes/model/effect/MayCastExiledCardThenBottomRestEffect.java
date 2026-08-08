package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Internal marker effect used in a {@code PendingMayAbility} to offer "you may cast that card
 * without paying its mana cost" for a card sitting in exile, and — whether the offer is accepted or
 * declined — then put every other card exiled with {@code sourcePermanentId} on the bottom of its
 * owner's library in a random order. Used by Possibility Storm.
 */
public record MayCastExiledCardThenBottomRestEffect(UUID sourcePermanentId) implements CardEffect {
}
