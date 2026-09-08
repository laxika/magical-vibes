package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each matching creature deals damage equal to its power to itself. Each creature is its own
 * damage source.
 */
public record EachCreatureDealsPowerDamageToItselfEffect(PermanentPredicate predicate) implements CardEffect {

    /** Applies to every creature. */
    public EachCreatureDealsPowerDamageToItselfEffect() {
        this(null);
    }
}
