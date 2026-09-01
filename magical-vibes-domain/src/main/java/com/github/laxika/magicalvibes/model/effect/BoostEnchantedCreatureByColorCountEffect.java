package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura boost: the enchanted creature gets +{@code powerPerColor}/+{@code
 * toughnessPerColor} for each of its colors (Blessing of the Nephilim).
 */
public record BoostEnchantedCreatureByColorCountEffect(
        int powerPerColor,
        int toughnessPerColor
) implements CardEffect {
}
