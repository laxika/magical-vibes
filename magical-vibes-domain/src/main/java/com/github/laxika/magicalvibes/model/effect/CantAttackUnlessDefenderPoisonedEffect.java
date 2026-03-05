package com.github.laxika.magicalvibes.model.effect;

/**
 * Static attacking restriction: this creature can't attack unless the defending player
 * has at least one poison counter.
 */
public record CantAttackUnlessDefenderPoisonedEffect() implements CardEffect {
}
