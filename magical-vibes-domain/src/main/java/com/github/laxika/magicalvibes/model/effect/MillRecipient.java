package com.github.laxika.magicalvibes.model.effect;

/** Who mills cards when a {@link MillEffect} resolves, relative to the effect's controller. */
public enum MillRecipient {
    CONTROLLER,
    TARGET_PLAYER,
    EACH_OPPONENT,
    /**
     * The controller of the spell targeted by this stack entry (i.e. {@code entry.getTargetId()}
     * resolves a spell on the stack, and that spell's controller mills). Used by "counter target
     * spell ... that spell's controller mills N cards" effects such as Broken Ambitions. Not a
     * chosen player target, so it never contributes a player target.
     */
    TARGET_SPELL_CONTROLLER
}
