package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Additional cast cost that reveals a matching card from the caster's hand without moving it.
 */
public record RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue,
                                    boolean optional)
        implements CostEffect {

    public RevealCardFromHandCost(CardPredicate predicate, String label) {
        this(predicate, label, false, false);
    }

    public RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue) {
        this(predicate, label, trackManaValue, false);
    }

    /** Creates an optional additional cost that reveals a matching card without moving it. */
    public static RevealCardFromHandCost optional(CardPredicate predicate, String label) {
        return new RevealCardFromHandCost(predicate, label, false, true);
    }
}
