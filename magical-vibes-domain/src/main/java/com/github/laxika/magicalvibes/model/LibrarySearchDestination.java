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
    /** Like {@link #EXILE_PLAYABLE}, but the play permission lasts only until the searcher's next
     *  upkeep; if the card is still exiled then, it is put into its owner's graveyard (Grinning Totem). */
    EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP,
    TOP_OF_LIBRARY,
    GRAVEYARD,
    BATTLEFIELD_ATTACHED_TO_PLAYER,
    BATTLEFIELD_ATTACHED_TO_CREATURE,
    SPHINX_AMBASSADOR,
    CAST_WITHOUT_PAYING,
    /** Put the chosen card onto the battlefield under the searching player's control (Bribery). */
    BATTLEFIELD_UNDER_SEARCHER,
    /** Aladdin's Lamp — put the chosen looked-at card back on top of the library and the rest on the
     *  bottom in a random order, then draw a card (the chosen one). The final draw is a real draw
     *  event (fires draw triggers), routed back through {@code DrawService.resolveDrawCard}. */
    DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM
}
