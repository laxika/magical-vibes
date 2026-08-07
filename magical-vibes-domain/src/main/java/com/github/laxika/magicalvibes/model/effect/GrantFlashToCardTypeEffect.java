package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: controller may cast spells matching the given predicate as though they had flash.
 * When {@code filter} is {@code null}, grants flash to all spell types (e.g. Leyline of Anticipation).
 * Used by Shimmer Myr (CardTypePredicate(ARTIFACT)), Leyline of Anticipation (null = all types),
 * Raff Capashen, Ship's Mage (CardIsHistoricPredicate), etc.
 *
 * <p>{@code appliesToAllPlayers} widens the grant to every player rather than only the source's
 * controller — "Any player may cast creature spells … as though they had flash" (Aluren).
 */
public record GrantFlashToCardTypeEffect(CardPredicate filter, boolean appliesToAllPlayers) implements CardEffect {

    public GrantFlashToCardTypeEffect(CardPredicate filter) {
        this(filter, false);
    }
}
