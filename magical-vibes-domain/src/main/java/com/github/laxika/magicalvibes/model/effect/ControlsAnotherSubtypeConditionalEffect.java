package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Intervening-if conditional wrapper: "if you control another [subtype]".
 * Checked at both trigger time and resolution time per CR 603.4.
 * The condition is met when the controller has at least one other permanent
 * with the specified subtype on the battlefield besides the source permanent.
 */
public record ControlsAnotherSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "controls another " + subtype.getDisplayName();
    }

    @Override
    public String conditionNotMetReason() {
        return "controller does not control another " + subtype.getDisplayName();
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
