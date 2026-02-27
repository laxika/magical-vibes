package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller's life total can't change.
 * (They can't gain or lose life. They can't pay any amount of life except 0.)
 * Damage is still dealt but doesn't cause the life total to change.
 */
public record LifeTotalCantChangeEffect() implements CardEffect {
}
