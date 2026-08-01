package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the permanent that the source Aura is attached to. Resolved by
 * {@code DestroyEnchantedPermanentEffectHandler}. Used on
 * {@code ON_ENCHANTED_PERMANENT_TAPPED} (Spreading Algae), {@code ON_ANY_CREATURE_DIES}
 * (Yoke of the Damned), and {@code ON_ENCHANTED_CREATURE_DEALT_DAMAGE} (Mortal Wound).
 */
public record DestroyEnchantedPermanentEffect() implements CardEffect {
}
