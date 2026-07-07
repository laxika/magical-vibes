package com.github.laxika.magicalvibes.model.effect;

/**
 * Which kinds of source a {@link TargetingRestrictionEffect} restricts from targeting the permanent.
 */
public enum TargetingSourceKind {

    /** Only spells are restricted (abilities may still target). */
    SPELLS,

    /** Only activated/triggered abilities are restricted (spells may still target). */
    ABILITIES,

    /** Both spells and abilities are restricted (hexproof/shroud-style). */
    SPELLS_AND_ABILITIES
}
