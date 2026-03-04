package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect: the spell is exiled instead of going to the graveyard after resolution.
 * Analogous to {@link ShuffleIntoLibraryEffect} but for exile.
 */
public record ExileSpellEffect() implements CardEffect {
}
