package com.github.laxika.magicalvibes.model.effect;

/**
 * The resolving controller privately looks at the top card of each player's library.
 * Libraries remain unchanged, and players with empty libraries contribute no card.
 */
public record LookAtTopCardOfEachPlayersLibraryEffect() implements CardEffect {
}
