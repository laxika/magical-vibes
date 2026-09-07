package com.github.laxika.magicalvibes.model.effect;

/** Puts the source permanent into its owner's library at a fixed position from the top. */
public record PutSourcePermanentIntoLibraryNFromTopEffect(int position) implements CardEffect {
}
