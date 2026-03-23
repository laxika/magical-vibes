package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper: "if you control N or more [permanents matching predicate]".
 * The wrapped effect only resolves if the controller has at least {@code minCount}
 * permanents on the battlefield matching the given predicate.
 * Used as an intervening-if condition on triggered abilities (e.g.
 * "At the beginning of your end step, if you control four or more creatures, transform ~.").
 */
public record ControlsPermanentCountConditionalEffect(int minCount, PermanentPredicate filter, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controls " + minCount + " or more matching permanents";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls fewer than " + minCount + " matching permanents";
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
