package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Intervening-if conditional wrapper: "if you control another [subtype]".
 * Checked at both trigger time and resolution time per CR 603.4.
 * The condition is met when the controller has at least one other permanent
 * with any of the specified subtypes on the battlefield besides the source permanent.
 *
 * @param subtypes    the set of subtypes to check — condition is met if another permanent has ANY of these
 * @param nontokenOnly when {@code true}, only nontoken permanents satisfy the condition
 */
public record ControlsAnotherSubtypeConditionalEffect(Set<CardSubtype> subtypes, boolean nontokenOnly, CardEffect wrapped) implements ConditionalEffect {

    /** Convenience constructor for a single subtype, not restricted to nontokens. */
    public ControlsAnotherSubtypeConditionalEffect(CardSubtype subtype, CardEffect wrapped) {
        this(Set.of(subtype), false, wrapped);
    }

    /** Convenience constructor for a single subtype with nontoken flag. */
    public ControlsAnotherSubtypeConditionalEffect(CardSubtype subtype, boolean nontokenOnly, CardEffect wrapped) {
        this(Set.of(subtype), nontokenOnly, wrapped);
    }

    @Override
    public String conditionName() {
        String tokenQualifier = nontokenOnly ? "nontoken " : "";
        String subtypeNames = subtypes.stream()
                .map(CardSubtype::getDisplayName)
                .collect(Collectors.joining(" or "));
        return "controls another " + tokenQualifier + subtypeNames;
    }

    @Override
    public String conditionNotMetReason() {
        String tokenQualifier = nontokenOnly ? "nontoken " : "";
        String subtypeNames = subtypes.stream()
                .map(CardSubtype::getDisplayName)
                .collect(Collectors.joining(" or "));
        return "controller does not control another " + tokenQualifier + subtypeNames;
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
