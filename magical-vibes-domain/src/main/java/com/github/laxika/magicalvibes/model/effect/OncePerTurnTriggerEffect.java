package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for triggered abilities that "trigger only once each turn" (e.g. Ghoulish Procession).
 * The engine marks the source permanent when the wrapped ability first fires in a turn and skips
 * subsequent events for that permanent until the turn clears.
 */
public record OncePerTurnTriggerEffect(CardEffect wrapped) implements CardEffect {
}
