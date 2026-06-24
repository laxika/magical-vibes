package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: no player can cast spells from libraries.
 * Prevents casting spells from the top of a library (e.g. via Future Sight-style permissions)
 * and any other mechanism that casts spells from libraries.
 * Does not prevent playing lands from libraries (lands are not spells).
 * Used by Grafdigger's Cage (DKA) and similar effects.
 */
public record PlayersCantCastSpellsFromLibrariesEffect() implements CardEffect {
}
