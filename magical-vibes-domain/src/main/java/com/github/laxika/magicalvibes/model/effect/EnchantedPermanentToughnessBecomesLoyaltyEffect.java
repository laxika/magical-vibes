package com.github.laxika.magicalvibes.model.effect;

/**
 * Static Aura effect for Planeswalkerificate-style text: the enchanted permanent's base toughness
 * tracks its stored toughness-as-loyalty value while its power remains unchanged.
 */
public record EnchantedPermanentToughnessBecomesLoyaltyEffect() implements CardEffect {
}
