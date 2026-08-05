package com.github.laxika.magicalvibes.model.effect;

/**
 * Which object of a resolving {@link RedirectNextDamageEffect} fills one of its two roles — the
 * object whose incoming damage is redirected, and the object the redirected damage is dealt to.
 */
public enum RedirectRole {

    /** The ability's own source permanent ("this creature"). */
    SOURCE_PERMANENT,

    /** The ability's target — a permanent or, for the player-inclusive categories, a player. */
    TARGET,

    /** The ability's controller ("you"; for a creature's own ability, its owner). */
    CONTROLLER
}
