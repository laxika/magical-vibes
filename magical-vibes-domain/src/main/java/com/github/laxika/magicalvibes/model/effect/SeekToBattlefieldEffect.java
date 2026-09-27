package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Randomly moves one matching card from the controller's library onto the battlefield tapped. */
public record SeekToBattlefieldEffect(CardPredicate predicate) implements CardEffect {
}
