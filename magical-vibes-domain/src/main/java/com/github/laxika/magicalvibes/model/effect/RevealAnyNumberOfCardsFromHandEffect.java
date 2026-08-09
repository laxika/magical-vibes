package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller chooses any number of cards matching {@code filter} from their hand to reveal.
 * The number of cards actually revealed is stored on the stack entry for a following effect.
 */
public record RevealAnyNumberOfCardsFromHandEffect(CardPredicate filter) implements CardEffect {
}
