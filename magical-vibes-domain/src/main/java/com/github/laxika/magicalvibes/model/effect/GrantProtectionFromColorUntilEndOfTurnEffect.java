package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grants protection from a fixed, printed colour until end of turn.
 *
 * <p>Scope {@link Subjects#target()} is "target creature gains protection from [color]" (Willow
 * Priestess). Scope {@link Subjects#source()} is "this creature gains protection from [color]"
 * (Keeper of Kookus) — no player target choice; resolves against the ability's source permanent.
 * Scope {@link Subjects#otherCreaturesYouControl()} is the untargeted mass form: each creature the ability's
 * controller controls gains protection from the color (Dominaria's Judgment).
 *
 * <p>Unlike {@link GrantProtectionChoiceUntilEndOfTurnEffect} the colour is known at build time, so
 * no player choice is prompted; the grant is stored in
 * {@code Permanent.protectionFromColorsUntilEndOfTurn} and cleared by the turn cleanup.
 *
 * @param color     the colour the permanent gains protection from
 * @param predicate an optional narrowing predicate on the legal target (TARGET subject only;
 *                  e.g. "green creature"); {@code null} means any creature is legal
 * @param subject     {@link Subjects#target()}, {@link Subjects#source()}, or
 *                  {@link Subjects#otherCreaturesYouControl()}
 */
public record GrantProtectionFromColorUntilEndOfTurnEffect(
        CardColor color, PermanentPredicate predicate, PermanentSubject subject, TargetPredicate declaredTarget)
        implements CardEffect {

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color) {
        this(color, null, Subjects.target(), TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentPredicate predicate) {
        this(color, predicate, Subjects.target(), TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentSubject subject) {
        this(color, null, subject, TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentSubject subject,
                                                         TargetPredicate declaredTarget) {
        this(color, null, subject, declaredTarget);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.benign(declaredTarget, predicate);
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
