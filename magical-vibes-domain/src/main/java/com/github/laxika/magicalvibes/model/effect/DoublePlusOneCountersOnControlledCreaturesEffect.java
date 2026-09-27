package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Double the number of +1/+1 counters on each matching creature its controller controls.
 * Non-targeting: every matching creature with at least one +1/+1 counter gets that many more.
 */
public record DoublePlusOneCountersOnControlledCreaturesEffect(PermanentPredicate predicate)
        implements CardEffect, DoublingEffect {

    /** Applies to every creature the controller controls. */
    public DoublePlusOneCountersOnControlledCreaturesEffect() {
        this(null);
    }
}
