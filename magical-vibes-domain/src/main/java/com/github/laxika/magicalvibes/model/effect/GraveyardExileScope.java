package com.github.laxika.magicalvibes.model.effect;

/**
 * Scope selector for {@link ExileGraveyardCardsEffect}. Drives which graveyard(s) are affected,
 * how targets (if any) are chosen, and the derived {@code canTarget*} flags.
 */
public enum GraveyardExileScope {

    /** A player exiles {@code count} cards from their OWN graveyard (choosing when they have more). */
    OWN,

    /**
     * Exiles ALL cards matching the filter from the controller's OWN graveyard (Zombie Mob).
     * A {@code null} filter exiles the whole graveyard. {@code count} is unused.
     */
    OWN_ALL_MATCHING,

    /** Exiles one TARGET card from any player's graveyard (optionally type-restricted by the filter). */
    TARGET_CARDS_ANY_GRAVEYARD,

    /** Exiles {@code count} TARGET cards from an OPPONENT's graveyard (opponent-only targeting). */
    TARGET_CARDS_OPPONENT_GRAVEYARD,

    /** Exiles any number of TARGET cards from the controller's graveyard. */
    TARGET_CARDS_CONTROLLER_GRAVEYARD,

    /** Exiles the ENTIRE graveyard of a TARGET player. */
    TARGET_PLAYER_ENTIRE,

    /** Exiles the entire graveyard of the creature's controller supplied by a delayed death trigger. */
    DYING_CREATURE_CONTROLLER,

    /**
     * Exiles ALL cards matching the filter from a TARGET player's graveyard (Crypt Incursion).
     * A {@code null} filter exiles the whole graveyard. {@code count} is unused; the number of
     * cards exiled this way lands on the entry's event value for a following {@code EventValue}
     * reader.
     */
    TARGET_PLAYER_ALL_MATCHING,

    /** Exiles all cards from EVERY player's graveyard. */
    ALL_PLAYERS,

    /** Exiles all cards from every OPPONENT's graveyard. */
    ALL_OPPONENTS,

    /**
     * Each opponent keeps {@code count} cards of their choice in their graveyard and exiles the rest
     * (Watchers of the Dead). An opponent with {@code count} or fewer cards keeps them all.
     */
    EACH_OPPONENT_KEEP
}
