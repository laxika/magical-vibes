package com.github.laxika.magicalvibes.model;

public enum LibrarySearchDestination {
    HAND,
    BATTLEFIELD,
    BATTLEFIELD_TAPPED,
    EXILE_IMPRINT,
    EXILE,
    /** Exile the chosen card tracked "with" a source permanent (Endless Horizons). Carries the
     * {@code sourcePermanentId} and re-filters the library on each repeated pick. */
    EXILE_WITH_SOURCE,
    EXILE_PLAYABLE,
    TOP_OF_LIBRARY,
    GRAVEYARD,
    BATTLEFIELD_ATTACHED_TO_PLAYER,
    BATTLEFIELD_ATTACHED_TO_CREATURE,
    SPHINX_AMBASSADOR,
    CAST_WITHOUT_PAYING,
    /** Put the chosen card onto the battlefield under the searching player's control (Bribery). */
    BATTLEFIELD_UNDER_SEARCHER
}
