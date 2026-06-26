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
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetSpell() {
        return wrapped.canTargetSpell();
    }

    @Override
    public boolean canTargetGraveyard() {
        return wrapped.canTargetGraveyard();
    }
}
