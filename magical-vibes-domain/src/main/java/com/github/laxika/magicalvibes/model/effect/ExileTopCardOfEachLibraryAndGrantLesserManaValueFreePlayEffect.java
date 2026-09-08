package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library, then lets the controller play their own exiled
 * card and each other exiled card with strictly lesser mana value without paying mana costs until
 * end of turn.
 */
public record ExileTopCardOfEachLibraryAndGrantLesserManaValueFreePlayEffect() implements CardEffect {
}
