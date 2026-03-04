package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can't attack or block unless it's equipped.
 * Used by Training Drone and similar cards.
 */
public record CantAttackOrBlockUnlessEquippedEffect() implements CardEffect {
}
