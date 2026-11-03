package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a triggered ability that fires only the first time each creature is affected during
 * a turn. By default the counter-placement watcher keys the limit by the source permanent and the
 * affected creature, so separate creatures can each trigger the wrapped ability once. A global
 * wrapper uses the affected creature's turn-wide event history instead, regardless of which source
 * was present when earlier events happened. Don't author the default form on a damage or counter
 * trigger: {@code TriggerLimit.ONCE_EACH_TURN_FOR_EACH_CREATURE} says it, and the legacy view
 * ({@code TriggeredAbility.legacyEffects()}) gives it to the collectors.
 */
public record OncePerTurnPerCreatureTriggerEffect(CardEffect wrapped, boolean global) implements CardEffect {

    public OncePerTurnPerCreatureTriggerEffect(CardEffect wrapped) {
        this(wrapped, false);
    }
}
