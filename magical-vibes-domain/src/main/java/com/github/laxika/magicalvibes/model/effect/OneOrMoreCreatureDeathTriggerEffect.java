package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Wraps a creature-death effect whose trigger condition is "one or more" matching creatures dying
 * simultaneously.
 */
public record OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped,
                                                   PermanentPredicate dyingPermanentPredicate)
        implements BatchedCreatureDeathTriggerEffect {

    public OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped) {
        this(wrapped, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
