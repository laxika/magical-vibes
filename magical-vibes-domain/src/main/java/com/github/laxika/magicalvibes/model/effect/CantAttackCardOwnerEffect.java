package com.github.laxika.magicalvibes.model.effect;

/**
 * Static restriction: this creature can't attack its card owner or planeswalkers that player
 * controls.
 */
public record CantAttackCardOwnerEffect() implements CardEffect {
}
