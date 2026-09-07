package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved as an exile trigger, puts the source card from its owner's exile zone into that
 * owner's library at {@code position} cards from the top (0-indexed).
 */
public record PutSourceCardFromExileIntoLibraryNFromTopEffect(int position) implements CardEffect {
}
