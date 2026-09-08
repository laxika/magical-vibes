package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Reveals exactly X cards matching {@code filter} from the controller's hand as an activation cost. */
public record RevealXCardsFromHandCost(CardPredicate filter) implements HandRevealCost {
}
