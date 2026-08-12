package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper for triggers whose event subject is a permanent.
 * The wrapped effect fires only if the triggering permanent matches {@code predicate}.
 * Controller-scoped enter triggers use the default constructor; the any-controller form is for
 * {@code ON_ANY_PERMANENT_ENTERS_BATTLEFIELD} abilities that watch permanents entering under any
 * player's control.
 */
public record TriggeringPermanentConditionalEffect(
        PermanentPredicate predicate,
        CardEffect wrapped,
        boolean anyController
) implements CardEffect {

    public TriggeringPermanentConditionalEffect(PermanentPredicate predicate, CardEffect wrapped) {
        this(predicate, wrapped, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
