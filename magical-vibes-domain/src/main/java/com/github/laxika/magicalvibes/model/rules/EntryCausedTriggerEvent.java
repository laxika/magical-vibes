package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.Objects;

/**
 * A permanent entering the battlefield caused {@code triggeredAbility} to trigger, now on the stack: the event
 * "whenever a permanent entering causes a triggered ability to trigger" abilities trigger on (CR 603.2). It is an
 * occurrence of its own for each such ability, emitted once that ability is on the stack.
 */
public record EntryCausedTriggerEvent(StackEntry triggeredAbility) implements RulesEvent {

    public EntryCausedTriggerEvent {
        Objects.requireNonNull(triggeredAbility, "triggeredAbility");
    }

    @Override
    public EntryCausedTriggerEvent deepCopy() {
        return new EntryCausedTriggerEvent(new StackEntry(triggeredAbility));
    }
}
