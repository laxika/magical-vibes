package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library, then lets the controller cast any number of the
 * exiled spells without paying their mana costs.
 */
public record ExileTopCardOfEachLibraryAndMayCastSpellsEffect() implements CardEffect {
}
