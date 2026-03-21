package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: controller may cast spells matching the given predicate as though they had flash.
 * When {@code filter} is {@code null}, grants flash to all spell types (e.g. Leyline of Anticipation).
 * Used by Shimmer Myr (CardTypePredicate(ARTIFACT)), Leyline of Anticipation (null = all types),
 * Raff Capashen, Ship's Mage (CardIsHistoricPredicate), etc.
 */
public record GrantFlashToCardTypeEffect(CardPredicate filter) implements CardEffect {
}
