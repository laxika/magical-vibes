package com.github.laxika.magicalvibes.model.trigger;

/**
 * A state trigger: the ability triggers when a game state is true rather than when an event happens, and doesn't
 * trigger again until it has left the stack (CR 603.8). The game state is the condition of the ability's
 * {@code StateTriggerEffect}, its only kind of effect. Build it through {@link Triggers}.
 */
public record StateTrigger() implements Trigger {
}
