package com.github.laxika.magicalvibes.model.effect;

/**
 * The enchanted creature deals damage equal to the amount it was dealt (stored in xValue on the
 * stack entry) to its controller. Used by Spiteful Shadows on {@code ON_ENCHANTED_CREATURE_DEALT_DAMAGE}.
 */
public record EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect() implements CardEffect {
}
