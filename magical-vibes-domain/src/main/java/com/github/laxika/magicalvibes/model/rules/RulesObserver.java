package com.github.laxika.magicalvibes.model.rules;

import java.util.List;

/**
 * Watches a game's rules events and the triggered abilities put toward the stack. Only the test fixtures implement
 * it (the rules event recorder and the trigger ledger). A game has none by default, and simulation copies never carry
 * one.
 */
public interface RulesObserver {

    /** Where a triggered ability was put, or the question that leads to one. */
    enum Route {
        /** A {@code TRIGGERED_ABILITY} stack entry added to the stack. */
        STACK,
        /**
         * A further stack object of an ability that triggers an additional time (CR 603.3), added to the stack with
         * the entry reported on {@link #STACK}.
         */
        STACK_ADDITIONAL,
        /** A triggered ability held until the mana ability that triggered it finishes resolving. */
        MANA_ABILITY_QUEUE,
        /** A triggered ability held while a subgame is being played. */
        SUSPENDED_QUEUE,
        /**
         * A question asked with a {@code PendingMayAbility}: an entry's "you may" as it resolves, or an option of a
         * replacement effect, a cost or a resolving object. Never a triggered ability on its way to the stack.
         */
        MAY_ABILITY,
        /** A pending interaction, such as a trigger's target choice. */
        INTERACTION
    }

    /** {@code item} (a stack entry, a pending may ability or a pending interaction) was put on {@code route}. */
    default void triggerQueued(Route route, Object item) {
    }

    /** The trigger pipeline took these batches out of the game's {@link RulesEventLog}. */
    default void eventsDrained(List<RulesEventBatch> batches) {
    }
}
