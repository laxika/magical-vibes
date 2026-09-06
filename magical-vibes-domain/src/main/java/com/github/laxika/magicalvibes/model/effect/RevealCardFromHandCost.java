package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Additional cast cost that reveals a matching card from the caster's hand without moving it.
 * The revealed card's mana value or power can optionally be snapshotted into the spell's X value.
 */
public record RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue,
                                     boolean trackPower, boolean optional) implements CostEffect {

    public RevealCardFromHandCost(CardPredicate predicate, String label) {
        this(predicate, label, false, false, false);
    }

    public RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue) {
        this(predicate, label, trackManaValue, false, false);
    }

    public RevealCardFromHandCost(CardPredicate predicate, String label, boolean trackManaValue,
                                  boolean trackPower) {
        this(predicate, label, trackManaValue, trackPower, false);
    }

    /** Creates an optional additional cost that reveals a matching card without moving it. */
    public static RevealCardFromHandCost optional(CardPredicate predicate, String label) {
        return new RevealCardFromHandCost(predicate, label, false, false, true);
    }
}
