package com.github.laxika.magicalvibes.model.effect;

/** Who mills cards when a {@link MillEffect} resolves, relative to the effect's controller. */
public enum MillRecipient {
    CONTROLLER,
    /** The player remembered by the source permanent's as-enters choice. */
    CHOSEN_PLAYER,
    TARGET_PLAYER,
    /**
     * "That player": the player the trigger event was about, the stack entry's non-targeting {@code targetId} (the
     * player who lost life, Mindcrank). Not a chosen target.
     */
    TRIGGERING_PLAYER,
    /** The player whose upkeep is currently resolving; supplied by the trigger entry. */
    ACTIVE_PLAYER,
    EACH_OPPONENT,
    /** Every player, including the controller, mills simultaneously as one event. */
    EACH_PLAYER,
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
    TARGET_PERMANENT_CONTROLLER,
    /**
     * The controller of the permanent the trigger event was about ({@code StackEntry.triggeringPermanentId}) as the
     * ability resolves, or its last known controller once it has left the battlefield, or the controller the entry
     * recorded for a source that was never a permanent, or the controller snapshot in
     * {@code StackEntry.eventPlayerIds}: "that permanent's controller mills a card" (Mesmeric Orb), "that source's
     * controller mills that many cards" (Belltower Sphinx).
     */
    TRIGGERING_PERMANENT_CONTROLLER
}
