package com.github.laxika.magicalvibes.model.effect;

/**
 * Which object the creatures affected by a {@link MustAttackNextTurnEffect} are forced to attack.
 */
public enum TauntTarget {

    /** The ability's controller ("attack you"; Taunt). */
    EFFECT_CONTROLLER,

    /** The ability's own source permanent ("attack this permanent"; Gideon Jura's +2). */
    SOURCE_PERMANENT
}
