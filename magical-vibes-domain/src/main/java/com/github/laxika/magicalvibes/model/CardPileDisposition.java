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
    HAND
}
