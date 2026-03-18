package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper: "as long as an opponent controls a [subtype]".
 * The wrapped effect only applies while any opponent has at least one permanent
 * with the specified subtype on the battlefield.
 */
public record OpponentControlsSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "opponent controls a " + subtype.getDisplayName();
    }

    @Override
    public String conditionNotMetReason() {
        return "no opponent controls a " + subtype.getDisplayName();
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
