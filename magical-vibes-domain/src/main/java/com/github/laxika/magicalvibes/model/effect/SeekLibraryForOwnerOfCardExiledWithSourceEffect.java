package com.github.laxika.magicalvibes.model.effect;

/**
 * Seeks one card sharing a card type with a card exiled with the source permanent. The exiled
 * card's owner searches their own library, without a player choice or shuffle.
 */
public record SeekLibraryForOwnerOfCardExiledWithSourceEffect() implements CardEffect {
}
