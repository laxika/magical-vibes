package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static effect: the first spell cast by the source controller each turn that matches the
 * predicate can be cast as though it had flash.
 */
public record GrantFlashToFirstMatchingSpellEachTurnEffect(CardPredicate predicate)
        implements FirstMatchingSpellFlashGrant {
}
