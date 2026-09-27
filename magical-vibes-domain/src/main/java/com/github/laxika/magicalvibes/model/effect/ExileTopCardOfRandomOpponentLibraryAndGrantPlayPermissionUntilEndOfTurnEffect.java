package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top card of a random opponent's library, tracks it with the source permanent, and
 * lets the source controller play it until end of turn.
 */
public record ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect()
        implements CardEffect {
}
