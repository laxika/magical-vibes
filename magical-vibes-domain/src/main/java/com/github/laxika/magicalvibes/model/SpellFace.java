package com.github.laxika.magicalvibes.model;

/**
 * Which face of a card a spell is cast as, for an effect that lets a player cast a card with certain characteristics:
 * only the characteristics of the face being cast are evaluated (CR 716.3a for an Adventure, CR 712.8b for a modal
 * double-faced card's face, chosen before the card is put onto the stack).
 */
public enum SpellFace {
    /** The card itself: its front face, or the only face of a single-faced card. */
    CARD,
    /** An adventurer card's Adventure. */
    ADVENTURE,
    /** A modal double-faced card's back face. */
    BACK_FACE
}
