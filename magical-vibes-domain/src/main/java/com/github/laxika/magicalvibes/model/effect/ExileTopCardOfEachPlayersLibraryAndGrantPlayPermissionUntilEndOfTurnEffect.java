package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of each player's library and lets the controller play those cards until end
 * of turn. Nonland cards also allow mana of any color to be spent when casting them.
 */
public record ExileTopCardOfEachPlayersLibraryAndGrantPlayPermissionUntilEndOfTurnEffect()
        implements CardEffect {
}
