package com.github.laxika.magicalvibes.ai;

/**
 * Categories for classifying instant spells by their primary role.
 * Used by AI engines to determine optimal casting timing.
 */
public enum InstantCategory {
    /** Destroys, exiles, bounces, or damage-kills creatures/permanents */
    REMOVAL,
    /** Deals damage directly to players */
    BURN_TO_FACE,
    /** Draws cards, scrys, or provides other card advantage */
    CARD_ADVANTAGE,
    /** Boosts own creatures (pump spells, combat tricks) */
    COMBAT_TRICK,
    /** Counters spells on the stack */
    COUNTERSPELL,
    /** Anything that doesn't fit the above categories */
    OTHER
}
