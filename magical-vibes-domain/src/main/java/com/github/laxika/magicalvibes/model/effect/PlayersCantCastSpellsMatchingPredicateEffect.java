package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: no player can cast spells matching the supplied predicate.
 */
public record PlayersCantCastSpellsMatchingPredicateEffect(CardPredicate predicate) implements CardEffect {
}
