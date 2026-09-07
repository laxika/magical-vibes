package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Returns one specific exiled card to the player whose hand was bound when the effect was created.
 * Used by Ugin's Spirit token so each token remembers its own exiled card.
 */
public record ReturnExiledCardToHandEffect(UUID exiledCardId, UUID handPlayerId) implements CardEffect {
}
