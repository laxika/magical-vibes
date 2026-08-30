package com.github.laxika.magicalvibes.model.effect;

/**
 * {@code ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD} marker (Fruit of the First Tree): when the
 * enchanted creature dies, the Aura's controller gains life and draws cards equal to the creature's
 * last-known effective toughness.
 */
public record EnchantedCreatureDiesGainLifeAndDrawEqualToToughnessEffect() implements CardEffect {
}
