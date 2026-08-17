package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Additional cast cost that reveals a matching card from the caster's hand without moving it.
 */
public record RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue)
        implements CostEffect {

    public RevealCardFromHandCost(CardPredicate predicate, String label) {
        this(predicate, label, false);
    }
}
