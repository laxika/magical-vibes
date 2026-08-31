package com.github.laxika.magicalvibes.model;

public enum LibrarySearchDestination {
    HAND,
    /** Reveal the chosen card, return it to the library, then shuffle. */
    REVEAL_ONLY,
    BATTLEFIELD,
    BATTLEFIELD_TAPPED,
    EXILE_IMPRINT,
    EXILE,
    /** Exile the chosen card tracked "with" a source permanent (Endless Horizons). Carries the
     * {@code sourcePermanentId} and re-filters the library on each repeated pick. */
    EXILE_WITH_SOURCE,
    /** Exile any number of matching cards, then create one token for each selected card. */
    EXILE_AND_CREATE_TOKENS,
    /** Exile any number of matching cards and grant permission to cast each until end of turn. */
    EXILE_PLAYABLE_ANY_NUMBER,
    /** Exile the chosen card face down into a pile tracked "with" a source permanent, shuffling the
     * pile once the last of {@code remainingCount} picks is made (Mangara's Tome). Carries the
     * {@code sourcePermanentId}. */
    EXILE_FACE_DOWN_PILE,
    /** Exile one card face down with a source while returning a preselected remainder to the bottom randomly. */
    EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
    /** Exile two cards face down with a source while returning a preselected remainder to the bottom randomly. */
    EXILE_TWO_FACE_DOWN_REST_TO_BOTTOM_RANDOM,
    /** Exile one card face down with a source while putting a preselected remainder on the bottom in any order. */
    EXILE_ONE_FACE_DOWN_REST_TO_BOTTOM,
    /** Exile one card face down with a source while putting a preselected remainder into the target player's graveyard. */
    EXILE_ONE_FACE_DOWN_REST_TO_GRAVEYARD,
    /** Exile one card face up with permission to play it this turn, putting the rest on the bottom randomly. */
    EXILE_PLAYABLE_REST_TO_BOTTOM_RANDOM,
    EXILE_PLAYABLE,
    /** Like {@link #EXILE_PLAYABLE}, but the play permission lasts only until the searcher's next
     *  upkeep; if the card is still exiled then, it is put into its owner's graveyard (Grinning Totem). */
    EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP,
    TOP_OF_LIBRARY,
    GRAVEYARD,
    BATTLEFIELD_ATTACHED_TO_PLAYER,
    BATTLEFIELD_ATTACHED_TO_CREATURE,
    /** Put the chosen card (an Aura) onto the battlefield attached to a specific permanent named by
     *  {@link LibrarySearchParams#attachToPermanentId()} (Sovereigns of Lost Alara). Unlike
     *  {@link #BATTLEFIELD_ATTACHED_TO_CREATURE} the host is fixed, not chosen by the searcher. */
    BATTLEFIELD_ATTACHED_TO_PERMANENT,
    SPHINX_AMBASSADOR,
    CAST_WITHOUT_PAYING,
    /** Offer the chosen exiled card for a free cast, or put it into its controller's hand. */
    CAST_WITHOUT_PAYING_OR_PUT_INTO_HAND,
    /** Cast the chosen revealed card without paying, then shuffle the rest into the library. */
    CAST_WITHOUT_PAYING_AND_SHUFFLE_LIBRARY,
    /** Discover a qualifying card: cast it for free or put it into its controller's hand. */
    DISCOVER,
    CAST_ONE_AND_PUT_OTHER_INTO_HAND,
    PUT_ONE_INTO_HAND_REST_TO_BOTTOM_RANDOM,
    /** Exile one chosen card, then offer it for casting without paying its mana cost. */
    EXILE_AND_MAY_CAST_WITHOUT_PAYING,
    /** Exile one chosen card face down and offer an eligible card for a free cast or its owner's hand. */
    EXILE_FACE_DOWN_AND_MAY_CAST_OR_PUT_INTO_HAND,
    /** Exile the chosen card face up and hold it for a later free cast, then continue the
    *  {@code PendingEachPlayerLibraryExile} queue (Jace, Architect of Thought's −8). */
    EXILE_FOR_FREE_CAST,
    /** Exile the chosen card face up and offer its controller a one-time free cast. */
    EXILE_FOR_MAY_CAST,
    /** Put the chosen card onto the battlefield under the searching player's control (Bribery). */
    BATTLEFIELD_UNDER_SEARCHER,
    /** Put the chosen card onto the battlefield tapped under a specified player's control. */
    BATTLEFIELD_TAPPED_UNDER_TARGET_PLAYER,
    /** Gifts Ungiven — the chosen cards are held out of every zone until an opponent has chosen
     *  which two of them go to the graveyard; the rest then go to the searcher's hand. The pool is
     *  carried in {@link LibrarySearchParams#accumulatedCards()} and handed to the opponent as a
     *  {@link PendingPileSeparation} with {@link CardPileDisposition#GIFTS_UNGIVEN}. */
    GIFTS_UNGIVEN_POOL,
    /** Signal the Clans — the revealed creature cards are held out of every zone in
     *  {@link LibrarySearchParams#accumulatedCards()} until the search ends. If exactly three cards
     *  with different names were revealed, one of them is chosen at random and put into the
     *  searcher's hand; every other revealed card is shuffled back into the library. */
    SIGNAL_THE_CLANS_POOL,
    /** Aladdin's Lamp — put the chosen looked-at card back on top of the library and the rest on the
     *  bottom in a random order, then draw a card (the chosen one). The final draw is a real draw
     *  event (fires draw triggers), routed back through {@code DrawService.resolveDrawCard}. */
    DRAW_CHOSEN_REST_TO_BOTTOM_RANDOM
}
