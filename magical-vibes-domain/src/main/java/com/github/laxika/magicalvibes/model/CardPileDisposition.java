package com.github.laxika.magicalvibes.model;

/**
 * How the chosen / unchosen piles of a pile separation ({@link PendingPileSeparation}) are
 * disposed of once the controller has picked a pile.
 */
public enum CardPileDisposition {
    /** Boneyard Parley: chosen pile → battlefield under the controller; other pile → owners' graveyards. */
    BATTLEFIELD,
    /** Brilliant Ultimatum: chosen pile is offered to be played/cast for free from exile; the rest stay exiled. */
    PLAY_FROM_EXILE,
    /** Fact-or-Fiction style (Unesh, Criosphinx Sovereign): chosen pile → controller's hand; other pile → controller's graveyard. */
    HAND,
    /** Intrude on the Mind: chosen pile → controller's hand; other pile → graveyard, then create a Thopter with counters. */
    HAND_AND_THOPTER,
    /** Curator of Destinies and Fortune's Favor: one pile is face down; the chosen pile goes to hand and the other to the graveyard. */
    HAND_WITH_FACE_DOWN_PILE,
    /** The Celestial Toymaker: the chosen pile goes to hand and the other remains exiled, with one pile face down. */
    HAND_AND_EXILE_WITH_FACE_DOWN_PILE,
    /**
     * Phyrexian Portal: the pile the controller picks is searched for one card to put into their
     * hand (the rest of that pile is shuffled into their library); the other pile is exiled. The
     * piles stay face down to the controller while they pick, so both are described by card count.
     */
    SEARCH_ONE_TO_HAND,
    /** Jace, Architect of Thought −2: chosen pile → controller's hand; other pile → the bottom of their library in any order. */
    HAND_AND_BOTTOM,
    /** Truth or Tale: one card from the chosen pile → controller's hand; every other card → the bottom of their library in any order. */
    ONE_FROM_CHOSEN_HAND_AND_BOTTOM,
    /**
     * Gifts Ungiven: the opponent's selection is not a pile split but a direct disposal — the cards
     * they choose go to the controller's graveyard and every other card goes to the controller's
     * hand, so the flow completes in step 1 and never asks the controller to pick a pile.
     */
    GIFTS_UNGIVEN,
    /** Threats Undetected: selected cards return to the controller's library; the rest go to hand. */
    THREATS_UNDETECTED,
    /** Elemental Teachings: selected cards go to the graveyard and the rest enter tapped. */
    GIFTS_UNGIVEN_BATTLEFIELD_TAPPED,
    /** Deliver Unto Evil: the opponent chooses cards to leave in the controller's graveyard. */
    DELIVER_UNTO_EVIL,
    /** Death or Glory: the opponent chooses which pile is exiled; the other returns to the battlefield. */
    OPPONENT_CHOOSES_EXILE,
    /** Fight or Flight: the chosen pile contains the only creatures that can attack this turn. */
    ATTACKERS,
    /** Stand or Fall: the chosen pile contains the only creatures that can block this turn. */
    BLOCKERS,
    /** Do or Die: target player chooses the pile whose creatures are destroyed without regeneration. */
    DESTROY,
    /** Abstract Performance: the chosen pile goes to the controller's graveyard; the other is offered for one free spell cast and then the rest go to hand. */
    GRAVEYARD_AND_FREE_CAST_ONE_REST_TO_HAND
}
