package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper: "as long as any player controls a permanent matching [filter]".
 * The wrapped effect only applies while at least one permanent on the battlefield
 * (from any player) matches the given predicate.
 */
public record AnyPlayerControlsPermanentConditionalEffect(PermanentPredicate filter, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "any player controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "no player controls a matching permanent";
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
    }

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
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
