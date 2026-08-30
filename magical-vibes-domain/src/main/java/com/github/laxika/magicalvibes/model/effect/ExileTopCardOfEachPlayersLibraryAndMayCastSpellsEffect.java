package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library, then lets the controller cast any number of
 * spells among those cards without paying their mana costs. Cards not cast remain in exile.
 */
public record ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect() implements CardEffect {
}
