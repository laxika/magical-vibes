package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Each player reveals any number of matching cards from their hand, then creates one copy of
 * {@code token} for each card they revealed.
 */
public record EachPlayerRevealsAnyNumberOfCardsFromHandThenCreatesTokensEffect(
        CardPredicate filter, CreateTokenEffect token) implements CardEffect {
}
