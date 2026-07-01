package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Wrapper for spell effects that apply only if the controller has cast another spell
 * matching {@code filter} this turn (excluding the resolving spell).
 * Checked at resolution time (intervening-if).
 */
public record ControllerCastAnotherSpellThisTurnConditionalEffect(
        CardPredicate filter,
        CardEffect wrapped
) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "another matching spell cast this turn";
    }

    @Override
    public String conditionNotMetReason() {
        return "you haven't cast another matching spell this turn";
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
