package com.github.laxika.magicalvibes.model.effect;

/**
 * The condition gating a {@link DamageLifeFloorEffect}. Evaluated by the engine against the
 * protected player immediately before the damage reduces their life total.
 */
public enum LifeFloorCondition {

    /** Unconditional — the floor always applies while the source is on the battlefield (Sustaining Spirit). */
    ALWAYS,

    /** The player controls at least one creature (Worship). */
    CONTROLS_A_CREATURE,

    /** The player's life total is already at or above the floor (Elderscale Wurm). */
    LIFE_AT_LEAST_FLOOR
}
