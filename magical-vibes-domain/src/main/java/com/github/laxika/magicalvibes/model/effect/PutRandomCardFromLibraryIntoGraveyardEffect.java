package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Puts one random card matching the predicate from the controller's library into their graveyard. */
public record PutRandomCardFromLibraryIntoGraveyardEffect(CardPredicate predicate) implements CardEffect {
}
