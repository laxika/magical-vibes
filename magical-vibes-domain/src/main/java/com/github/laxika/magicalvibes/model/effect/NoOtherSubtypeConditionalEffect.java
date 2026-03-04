package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Intervening-if conditional wrapper: "if you control no [subtype] other than this creature".
 * Checked at both trigger time and resolution time per CR 603.4.
 * The condition is met when the controller has no other permanents with the given subtype
 * on the battlefield besides the source permanent.
 */
public record NoOtherSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "no other " + subtype.getDisplayName() + "s";
    }

    @Override
    public String conditionNotMetReason() {
        return "controller controls other " + subtype.getDisplayName() + "s";
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
