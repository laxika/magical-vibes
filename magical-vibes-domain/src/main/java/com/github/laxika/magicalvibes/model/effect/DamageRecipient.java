package com.github.laxika.magicalvibes.model.effect;

/** Which player(s) a {@link DealDamageToPlayersEffect} deals its damage to. */
public enum DamageRecipient {
    /** The single player targeted by the effect ({@code entry.getTargetId()}); the only recipient that targets. */
    TARGET_PLAYER,
    /** Every opponent of the effect's controller. */
    EACH_OPPONENT,
    /** Every player (including the controller). */
    EACH_PLAYER,
    /** The player whose upkeep is currently resolving; supplied by the trigger entry. */
    ACTIVE_PLAYER,
    /** The effect's controller ("deals N damage to you"). */
    CONTROLLER,
    /** The enchanted player of a curse ({@code entry.getTargetId()} baked at trigger time; not chosen). */
    ENCHANTED_PLAYER,
    /** The controller of the permanent an Aura is attached to ({@code entry.getTargetId()} baked at trigger time; e.g. Feedback). */
    ENCHANTED_PERMANENT_CONTROLLER,
    /** The controller of the targeted permanent ("… and N damage to that creature's controller"). */
    TARGET_PERMANENT_CONTROLLER,
    /**
     * The controller of the targeted spell on the stack ({@code entry.getTargetId()} is that spell's
     * card id). Pair with {@code TargetSpellManaValue} for "damage equal to that spell's mana value"
     * (Refuse), or {@code TargetSpellPower} for "damage equal to that spell's power" (Essence Backlash).
     */
    TARGET_SPELL_CONTROLLER,
    /** The controller of the permanent that caused the trigger ({@code entry.getTargetId()} baked at trigger time). */
    TRIGGERING_PERMANENT_CONTROLLER,
    /**
     * The player whose action caused the trigger — "that player" ({@code entry.getTargetId()} baked
     * at trigger time, e.g. the caster of the triggering spell for Ash Zealot). Does not target.
     */
    TRIGGERING_PLAYER
}
