package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: the first spell cast by the source controller each turn that matches the
 * predicate costs the given amount less generic mana. When {@code kickedOnly} is true, only
 * kicked spells qualify and consume the once-per-turn allowance.
 */
public record ReduceCastCostForFirstMatchingSpellEachTurnEffect(
        CardPredicate predicate, int amount, boolean kickedOnly
) implements CardEffect {

    public ReduceCastCostForFirstMatchingSpellEachTurnEffect(CardPredicate predicate, int amount) {
        this(predicate, amount, false);
    }
}
