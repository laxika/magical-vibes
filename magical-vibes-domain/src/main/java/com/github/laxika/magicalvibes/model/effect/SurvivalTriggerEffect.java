package com.github.laxika.magicalvibes.model.effect;

/**
 * Wrapper for a Survival ability, which is evaluated only at the first postcombat main phase
 * while its source permanent remains on the battlefield.
 */
public record SurvivalTriggerEffect(CardEffect wrapped) implements CardEffect {
}
