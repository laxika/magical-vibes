package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly seeks one matching card from the controller's library onto the battlefield. */
public record SeekCardToBattlefieldEffect(CardPredicate filter, boolean enterTapped) implements CardEffect {
}
