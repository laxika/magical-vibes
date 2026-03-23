package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Intervening-if conditional wrapper: "if you control another [subtype]".
 * Checked at both trigger time and resolution time per CR 603.4.
 * The condition is met when the controller has at least one other permanent
 * with the specified subtype on the battlefield besides the source permanent.
 *
 * @param nontokenOnly when {@code true}, only nontoken permanents satisfy the condition
 */
public record ControlsAnotherSubtypeConditionalEffect(CardSubtype subtype, boolean nontokenOnly, CardEffect wrapped) implements ConditionalEffect {

    /** Backward-compatible constructor that does not restrict to nontokens. */
    public ControlsAnotherSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) {
        this(subtype, false, wrapped);
    }

    @Override
    public String conditionName() {
        String tokenQualifier = nontokenOnly ? "nontoken " : "";
        return "controls another " + tokenQualifier + subtype.getDisplayName();
    }

    @Override
    public String conditionNotMetReason() {
        String tokenQualifier = nontokenOnly ? "nontoken " : "";
        return "controller does not control another " + tokenQualifier + subtype.getDisplayName();
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
