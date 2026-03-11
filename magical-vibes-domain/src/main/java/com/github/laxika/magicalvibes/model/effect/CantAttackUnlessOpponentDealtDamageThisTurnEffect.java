package com.github.laxika.magicalvibes.model.effect;

/**
 * Static attacking restriction: this creature can't attack unless an opponent
 * has been dealt damage this turn (from any source — combat, spells, abilities).
 */
public record CantAttackUnlessOpponentDealtDamageThisTurnEffect() implements CardEffect {
}
