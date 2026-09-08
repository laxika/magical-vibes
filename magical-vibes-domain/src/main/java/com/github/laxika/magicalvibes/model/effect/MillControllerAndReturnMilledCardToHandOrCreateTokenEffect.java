package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then puts one matching milled card into their hand.
 * If no matching card was milled, the fallback token is created instead.
 */
public record MillControllerAndReturnMilledCardToHandOrCreateTokenEffect(
        int count, CardPredicate filter, CreateTokenEffect fallbackToken) implements CardEffect {
}
