package com.github.laxika.magicalvibes.model;

/**
 * How the chosen / unchosen piles of a card-pile separation ({@link PendingPileSeparation}) are
 * disposed of once the controller has picked a pile.
 */
public enum CardPileDisposition {
    /** Boneyard Parley: chosen pile → battlefield under the controller; other pile → owners' graveyards. */
    BATTLEFIELD,
    /** Brilliant Ultimatum: chosen pile is offered to be played/cast for free from exile; the rest stay exiled. */
    PLAY_FROM_EXILE,
    /** Fact-or-Fiction style (Unesh, Criosphinx Sovereign): chosen pile → controller's hand; other pile → controller's graveyard. */
    HAND,
    /**
     * Phyrexian Portal: the pile the controller picks is searched for one card to put into their
     * hand (the rest of that pile is shuffled into their library); the other pile is exiled. The
     * piles stay face down to the controller while they pick, so both are described by card count.
     */
    SEARCH_ONE_TO_HAND,
    /** Jace, Architect of Thought −2: chosen pile → controller's hand; other pile → the bottom of their library in any order. */
    HAND_AND_BOTTOM
}
