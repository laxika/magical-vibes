package com.github.laxika.magicalvibes.model;

/**
 * The game zones a card or object can occupy (CR 400). Used by static restrictions and other
 * effects that need to reason about where a card currently is or which zones they apply to.
 */
public enum Zone {
    /** The shared battlefield where permanents (lands, creatures, artifacts, etc.) exist (CR 403). */
    BATTLEFIELD,
    /** A player's graveyard: their discard pile of dead creatures, used cards, and discards (CR 404). */
    GRAVEYARD,
    /** A player's library: their face-down deck cards are drawn from (CR 401). */
    LIBRARY,
    /** The shared stack where spells and abilities wait to resolve, last in first out (CR 405). */
    STACK,
    /** Exile: a holding zone for cards removed from the game, face up unless stated otherwise (CR 406). */
    EXILE,
    /** A player's hand: cards they can normally cast or play from (CR 402). */
    HAND,
    /** A player's command zone: holds their commander(s) and similar objects (CR 903.6). */
    COMMAND
}
