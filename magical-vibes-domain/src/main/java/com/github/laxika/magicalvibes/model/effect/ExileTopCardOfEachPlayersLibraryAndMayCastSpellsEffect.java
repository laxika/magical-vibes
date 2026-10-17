package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library, then lets the controller cast one spell among
 * those cards, paying its mana cost. Cards not cast remain in exile.
 */
public record ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect() implements CardEffect {
}
