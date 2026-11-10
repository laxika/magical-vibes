package com.github.laxika.magicalvibes.service.trigger;

/**
 * Functional interface for trigger collector handlers.
 * <p>
 * Each handler puts one triggered ability that triggered on the stack, whole (CR 603.3), and returns whether it did.
 */
@FunctionalInterface
public interface TriggerCollectorHandler {
    /**
     * @param match   the ability: its source, its controller and its effects, in order
     * @param context event-specific context (SpellCast, LandTap, etc.)
     * @return {@code true} if a trigger was produced, {@code false} if conditions were not met
     */
    boolean handle(TriggerMatchContext match, TriggerContext context);
}
