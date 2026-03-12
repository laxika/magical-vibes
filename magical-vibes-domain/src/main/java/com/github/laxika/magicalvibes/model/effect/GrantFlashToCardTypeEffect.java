package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Static effect: controller may cast spells of the given card type as though they had flash.
 * When {@code cardType} is {@code null}, grants flash to all spell types (e.g. Leyline of Anticipation).
 * Used by Shimmer Myr (artifact spells), Teferi, Mage of Zhalfir (creature spells), etc.
 */
public record GrantFlashToCardTypeEffect(CardType cardType) implements CardEffect {
}
