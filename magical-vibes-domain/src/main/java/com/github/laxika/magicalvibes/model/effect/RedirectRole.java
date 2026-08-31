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
    CONTROLLER,

    /** The ability's source permanent and its controller ("this creature and/or you"). */
    SOURCE_PERMANENT_AND_CONTROLLER,

    /** The permanent the ability's Aura source is attached to ("enchanted creature"). */
    ENCHANTED_PERMANENT
}
