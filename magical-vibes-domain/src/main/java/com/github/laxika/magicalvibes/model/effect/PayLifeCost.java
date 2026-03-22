package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect: pay N life to activate an ability.
 * Validated and paid during ability activation in AbilityActivationService.
 */
public record PayLifeCost(int amount) implements CostEffect {
}
