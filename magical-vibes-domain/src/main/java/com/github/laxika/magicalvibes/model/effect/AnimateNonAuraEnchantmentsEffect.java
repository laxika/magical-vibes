package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that turns each other non-Aura enchantment into a creature with base power and
 * toughness each equal to its mana value, while retaining its other types and abilities.
 */
public record AnimateNonAuraEnchantmentsEffect() implements CardEffect {
}
