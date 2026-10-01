package com.github.laxika.magicalvibes.model.effect;

/**
 * Prevents creatures whose mana values have the parity chosen earlier in the current effect
 * sequence from blocking until end of turn.
 */
public record CreaturesOfChosenManaValueParityCantBlockThisTurnEffect() implements CardEffect {
}
