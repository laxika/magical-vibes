package com.github.laxika.magicalvibes.model.effect;

/**
 * Scope selector for {@link ExileGraveyardCardsEffect}. Drives which graveyard(s) are affected,
 * how targets (if any) are chosen, and the derived {@code canTarget*} flags.
 */
public enum GraveyardExileScope {

    /** A player exiles {@code count} cards from their OWN graveyard (choosing when they have more). */
    OWN,

    /** Exiles one TARGET card from any player's graveyard (optionally type-restricted by the filter). */
    TARGET_CARDS_ANY_GRAVEYARD,

    /** Exiles {@code count} TARGET cards from an OPPONENT's graveyard (opponent-only targeting). */
    TARGET_CARDS_OPPONENT_GRAVEYARD,

    /** Exiles the ENTIRE graveyard of a TARGET player. */
    TARGET_PLAYER_ENTIRE,

    /** Exiles all cards from EVERY player's graveyard. */
    ALL_PLAYERS,

    /** Exiles all cards from every OPPONENT's graveyard. */
    ALL_OPPONENTS
}
