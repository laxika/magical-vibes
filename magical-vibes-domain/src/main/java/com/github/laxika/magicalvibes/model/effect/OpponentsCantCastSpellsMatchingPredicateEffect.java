package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: opponents of the source permanent's controller can't cast spells matching the
 * supplied predicate.
 */
public record OpponentsCantCastSpellsMatchingPredicateEffect(CardPredicate predicate) implements CardEffect {
}
