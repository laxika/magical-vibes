package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Functional interface for trigger collector handlers.
 * <p>
 * Each handler processes a single (permanent, effect) match and returns whether the trigger fired.
 */
@FunctionalInterface
public interface TriggerCollectorHandler {
    /**
     * @param match       common match data (gameData, permanent, controllerId, rawEffect)
     * @param innerEffect the unwrapped effect (same as rawEffect unless MayEffect-wrapped)
     * @param context     event-specific context (SpellCast, LandTap, etc.)
     * @return {@code true} if a trigger was produced, {@code false} if conditions were not met
     */
    boolean handle(TriggerMatchContext match, CardEffect innerEffect, TriggerContext context);
}
