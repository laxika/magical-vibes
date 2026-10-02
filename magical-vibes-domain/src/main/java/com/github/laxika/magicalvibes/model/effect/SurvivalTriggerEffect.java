package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a Survival ability, evaluated at the beginning of each postcombat main phase
 * while its source permanent remains on the battlefield. Once-only abilities wrap their effect
 * with {@link OnceOnlyTriggerEffect} separately.
 */
public record SurvivalTriggerEffect(CardEffect wrapped) implements CardEffect {
}
