package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Lets the source card's owner put a matching card from their hand onto the battlefield. */
public record PutCardToBattlefieldForOwnerEffect(CardPredicate predicate, String label,
                                                  boolean enterTapped) implements CardEffect {
}
