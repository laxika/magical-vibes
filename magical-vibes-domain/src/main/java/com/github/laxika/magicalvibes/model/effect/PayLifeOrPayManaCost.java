package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost: pay a fixed amount of life or pay the listed mana cost.
 * Exactly one option is paid.
 */
public record PayLifeOrPayManaCost(int lifeAmount, String manaCost) implements CostEffect {
}
