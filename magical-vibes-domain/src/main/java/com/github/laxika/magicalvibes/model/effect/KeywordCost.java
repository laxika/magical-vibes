package com.github.laxika.magicalvibes.model.effect;

/**
 * A keyword's cost, or a special action's, that a {@link ModifyKeywordCostEffect} makes cheaper or more expensive.
 * Each is read where that cost is paid.
 */
public enum KeywordCost {

    /** The optional additional buyback cost of a spell (CR 702.27a). */
    BUYBACK,

    /** The dash alternative cost of a spell (CR 702.109a). */
    DASH,

    /** The flashback alternative cost of a spell cast from a graveyard (CR 702.34a). */
    FLASHBACK,

    /** The cost of a cycling ability (CR 702.29a). */
    CYCLING,

    /** The cost of a ninjutsu ability (CR 702.49a). */
    NINJUTSU,

    /**
     * A morph cost paid to turn a face-down permanent face up (CR 702.37e), megamorph's included (CR 702.37b); not
     * the {3} of casting a spell face down.
     */
    MORPH,

    /** The unlock cost of a Room's locked half, a special action (CR 709.5e). */
    UNLOCK
}
