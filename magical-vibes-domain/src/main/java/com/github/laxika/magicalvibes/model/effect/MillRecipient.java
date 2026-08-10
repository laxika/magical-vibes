package com.github.laxika.magicalvibes.model.effect;

/** Who mills cards when a {@link MillEffect} resolves, relative to the effect's controller. */
public enum MillRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    /** The player whose upkeep is currently resolving; supplied by the trigger entry. */
    ACTIVE_PLAYER,
    EACH_OPPONENT,
    /**
     * The controller of the spell targeted by this stack entry (i.e. {@code entry.getTargetId()}
     * resolves a spell on the stack, and that spell's controller mills). Used by "counter target
     * spell ... that spell's controller mills N cards" effects such as Broken Ambitions. Not a
     * chosen player target, so it never contributes a player target.
     */
    TARGET_SPELL_CONTROLLER,
    /**
     * The controller of the permanent targeted by this stack entry ({@code entry.getTargetId()} is a
     * permanent). Used by "destroy target land, its controller reveals ..." effects such as Destroy
     * the Evidence. Not a chosen player target, so it never contributes a player target.
     */
    TARGET_PERMANENT_CONTROLLER
}
