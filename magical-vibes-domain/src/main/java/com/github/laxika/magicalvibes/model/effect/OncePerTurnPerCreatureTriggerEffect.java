package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a triggered ability that fires only the first time each creature is affected during
 * a turn. The counter-placement watcher keys the limit by the source permanent and the affected
 * creature, so separate creatures can each trigger the wrapped ability once.
 */
public record OncePerTurnPerCreatureTriggerEffect(CardEffect wrapped) implements CardEffect {
}
