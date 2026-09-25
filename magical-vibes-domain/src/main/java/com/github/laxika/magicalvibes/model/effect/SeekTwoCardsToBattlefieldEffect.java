package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Seeks up to two matching cards, then puts one chosen card onto the battlefield. */
public record SeekTwoCardsToBattlefieldEffect(CardPredicate filter) implements CardEffect {
}
