package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper: "as long as you control a [subtype]".
 * The wrapped effect only applies while the controller has at least one permanent
 * with the specified subtype on the battlefield.
 */
public record ControlsSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controls a " + subtype.getDisplayName();
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control a " + subtype.getDisplayName();
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
