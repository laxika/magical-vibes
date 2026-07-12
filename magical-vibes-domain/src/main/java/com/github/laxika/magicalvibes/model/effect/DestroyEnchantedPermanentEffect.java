package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the permanent that the source Aura is attached to. Placed in the
 * {@code ON_ENCHANTED_PERMANENT_TAPPED} slot; resolved by {@code DestroyEnchantedPermanentEffectHandler}.
 * Used by Spreading Algae ("When enchanted land becomes tapped, destroy it").
 */
public record DestroyEnchantedPermanentEffect() implements CardEffect {
}
