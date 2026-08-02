package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Casts a copy of the exiled card identified by {@code encodedCardId} without paying its mana
 * cost. The encoded card itself remains in exile.
 */
public record CipherCastCopyEffect(UUID encodedCardId) implements CardEffect {
}
