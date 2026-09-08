package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grants protection from a fixed, printed colour until end of turn.
 *
 * <p>Scope {@link GrantScope#TARGET} is "target creature gains protection from [color]" (Willow
 * Priestess). Scope {@link GrantScope#SELF} is "this creature gains protection from [color]"
 * (Keeper of Kookus) — no player target choice; resolves against the ability's source permanent.
 * Scope {@link GrantScope#OWN_CREATURES} is the untargeted mass form: each creature the ability's
 * controller controls gains protection from the color (Dominaria's Judgment).
 *
 * <p>Unlike {@link GrantProtectionChoiceUntilEndOfTurnEffect} the colour is known at build time, so
 * no player choice is prompted; the grant is stored in
 * {@code Permanent.protectionFromColorsUntilEndOfTurn} and cleared by the turn cleanup.
 *
 * @param color     the colour the permanent gains protection from
 * @param predicate an optional narrowing predicate on the legal target (TARGET scope only;
 *                  e.g. "green creature"); {@code null} means any creature is legal
 * @param scope     {@link GrantScope#TARGET}, {@link GrantScope#SELF}, or
 *                  {@link GrantScope#OWN_CREATURES}
 */
public record GrantProtectionFromColorUntilEndOfTurnEffect(
        CardColor color, PermanentPredicate predicate, GrantScope scope, TargetPredicate declaredTarget)
        implements CardEffect {

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color) {
        this(color, null, GrantScope.TARGET, TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentPredicate predicate) {
        this(color, predicate, GrantScope.TARGET, TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, GrantScope scope) {
        this(color, null, scope, TargetPredicates.creature());
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, GrantScope scope,
                                                         TargetPredicate declaredTarget) {
        this(color, null, scope, declaredTarget);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(declaredTarget, predicate);
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
