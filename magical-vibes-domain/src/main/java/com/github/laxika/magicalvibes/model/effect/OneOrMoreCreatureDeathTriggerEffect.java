package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Wraps a creature-death effect whose trigger condition is "one or more" matching creatures dying
 * simultaneously.
 */
public record OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped,
                                                   PermanentPredicate dyingPermanentPredicate,
                                                   boolean includeSource)
        implements BatchedCreatureDeathTriggerEffect {

    public OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped) {
        this(wrapped, null, false);
    }

    public OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped, PermanentPredicate dyingPermanentPredicate) {
        this(wrapped, dyingPermanentPredicate, false);
    }

    public OneOrMoreCreatureDeathTriggerEffect(CardEffect wrapped, boolean includeSource) {
        this(wrapped, null, includeSource);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
