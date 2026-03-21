package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Put one or more +1/+1 counters on target creature.
 *
 * @param count number of +1/+1 counters to place
 * @param targetPredicate optional predicate restricting which creatures can be targeted
 */
public record PutPlusOnePlusOneCounterOnTargetCreatureEffect(int count, PermanentPredicate targetPredicate) implements CardEffect {

    public PutPlusOnePlusOneCounterOnTargetCreatureEffect(int count) {
        this(count, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public PermanentPredicate targetPredicate() {
        return targetPredicate;
    }
}
