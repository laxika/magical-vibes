package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Grants a one-shot zero-mana alternative cost to the next matching spell cast this turn. */
public record GrantFreeCastForNextMatchingSpellEffect(CardPredicate predicate) implements CardEffect {
}
