package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Intervening-if conditional wrapper: "if you control another permanent matching predicate".
 * The source permanent is excluded from the condition.
 */
public record ControlsAnotherPermanentConditionalEffect(
        PermanentPredicate filter,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controls another matching permanent";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control another matching permanent";
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

    @Override
    public boolean isDamageOrDestruction() {
        return wrapped.isDamageOrDestruction();
    }
}
