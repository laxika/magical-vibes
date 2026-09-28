package com.github.laxika.magicalvibes.model.effect;

/**
 * Allows the effect's controller to pay 2 life instead of paying each black mana symbol in a
 * cost. This changes how the cost may be paid; it does not reduce the cost or its mana value.
 */
public record MayPayLifeForBlackManaEffect() implements CardEffect {
}
