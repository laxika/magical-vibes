package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker for a creature-death trigger that fires once for a simultaneous event containing one or
 * more matching creatures.
 */
public interface BatchedCreatureDeathTriggerEffect extends CardEffect {

    CardEffect wrapped();

    /** Optional predicate restricting which dying permanents satisfy the batch. */
    default PermanentPredicate dyingPermanentPredicate() {
        return null;
    }
}
