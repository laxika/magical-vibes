package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper for triggers whose event subject is a permanent.
 * The wrapped effect fires only if the triggering permanent matches {@code predicate}.
 */
public record TriggeringPermanentConditionalEffect(
        PermanentPredicate predicate,
        CardEffect wrapped
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
