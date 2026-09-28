package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, damage from sources controlled by the resolving
 * effect's controller gets an additive bonus.
 */
public record BoostControllerDamageThisTurnEffect(int amount) implements CardEffect {
}
