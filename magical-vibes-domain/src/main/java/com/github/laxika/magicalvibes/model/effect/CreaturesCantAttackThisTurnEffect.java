package com.github.laxika.magicalvibes.model.effect;

/**
 * Turn-scoped global restriction: no creature can be declared as an attacker for the rest of
 * the current turn. The restriction is evaluated at attacker declaration, so it also covers
 * creatures that enter the battlefield after this effect resolves.
 */
public record CreaturesCantAttackThisTurnEffect() implements CardEffect {
}
