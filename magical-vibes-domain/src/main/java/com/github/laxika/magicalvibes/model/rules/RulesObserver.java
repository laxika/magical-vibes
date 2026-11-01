package com.github.laxika.magicalvibes.model.rules;

import java.util.List;

/**
 * Watches a game's rules events and the triggered abilities its collectors put toward the stack. Only the
 * test fixtures' shadow recorder implements it, to compare the slot collectors with the trigger pipeline
 * while event families move from one to the other. A game has none by default, and simulation copies never
 * carry one.
 */
public interface RulesObserver {

    /** Where a collector put a triggered ability, or the question that leads to one. */
    enum Route {
        /** A {@code TRIGGERED_ABILITY} stack entry added to the stack. */
        STACK,
        /** A triggered ability held until the mana ability that triggered it finishes resolving. */
        MANA_ABILITY_QUEUE,
        /** A triggered ability held while a subgame is being played. */
        SUSPENDED_QUEUE,
        /** A "may" question asked before an ability goes on the stack. */
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
