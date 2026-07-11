package com.github.laxika.magicalvibes.model.effect;

/** Which player(s) a {@link DealDamageToPlayersEffect} deals its damage to. */
public enum DamageRecipient {
    /** The single player targeted by the effect ({@code entry.getTargetId()}); the only recipient that targets. */
    TARGET_PLAYER,
    /** Every opponent of the effect's controller. */
    EACH_OPPONENT,
    /** Every player (including the controller). */
    EACH_PLAYER,
    /** The effect's controller ("deals N damage to you"). */
    CONTROLLER,
    /** The enchanted player of a curse ({@code entry.getTargetId()} baked at trigger time; not chosen). */
    ENCHANTED_PLAYER,
    /** The controller of the targeted permanent ("… and N damage to that creature's controller"). */
    TARGET_PERMANENT_CONTROLLER,
    /** The controller of the permanent that caused the trigger ({@code entry.getTargetId()} baked at trigger time). */
    TRIGGERING_PERMANENT_CONTROLLER
}
