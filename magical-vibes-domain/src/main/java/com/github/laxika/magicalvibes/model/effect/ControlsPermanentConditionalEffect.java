package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Conditional wrapper: "if you control a [permanent matching predicate]".
 * The wrapped effect only resolves if the controller has at least one permanent
 * on the battlefield matching the given predicate at resolution time.
 */
public record ControlsPermanentConditionalEffect(PermanentPredicate filter, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controls a matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a matching permanent";
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
