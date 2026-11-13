package com.github.laxika.magicalvibes.model.rules;

/**
 * A game event that triggered abilities can match (CR 603.2), recorded synchronously inside the action
 * that performs it. Unlike the domain events in {@code model.event}, which are post-mutation facts for
 * projection that no rules code may read, rules events exist for the trigger pipeline.
 *
 * <p>The engine emits rules events into {@link RulesEventLog}, which groups the events of one occurrence
 * into one {@link RulesEventBatch} (CR 603.2c).
 */
public sealed interface RulesEvent permits LibraryEvent, PermanentChangeEvent, ZoneChangeEvent {

    /** A copy that shares nothing mutable with this event, for simulation copies of the game. */
    RulesEvent deepCopy();
}
