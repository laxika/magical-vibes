package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: "Exile the top card of your library."
 * Used as part of activated abilities (e.g. Precognition Field's "{3}: Exile the
 * top card of your library.").
 */
public record ExileTopCardOfOwnLibraryEffect() implements CardEffect {
}
