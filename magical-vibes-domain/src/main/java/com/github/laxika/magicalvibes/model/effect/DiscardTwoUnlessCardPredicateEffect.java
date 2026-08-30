package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The controller discards two cards unless they discard one card matching the predicate instead.
 */
public record DiscardTwoUnlessCardPredicateEffect(CardPredicate predicate) implements CardEffect {
}
