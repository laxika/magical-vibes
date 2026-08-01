package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grants protection from a fixed, printed colour until end of turn.
 *
 * <p>Scope {@link GrantScope#TARGET} is "target creature gains protection from [color]" (Willow
 * Priestess). Scope {@link GrantScope#SELF} is "this creature gains protection from [color]"
 * (Keeper of Kookus) — no player target choice; resolves against the ability's source permanent.
 *
 * <p>Unlike {@link GrantProtectionChoiceUntilEndOfTurnEffect} the colour is known at build time, so
 * no player choice is prompted; the grant is stored in
 * {@code Permanent.protectionFromColorsUntilEndOfTurn} and cleared by the turn cleanup.
 *
 * @param color     the colour the permanent gains protection from
 * @param predicate an optional narrowing predicate on the legal target (TARGET scope only;
 *                  e.g. "green creature"); {@code null} means any creature is legal
 * @param scope     {@link GrantScope#TARGET} or {@link GrantScope#SELF}
 */
public record GrantProtectionFromColorUntilEndOfTurnEffect(
        CardColor color, PermanentPredicate predicate, GrantScope scope)
        implements CardEffect {

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color) {
        this(color, null, GrantScope.TARGET);
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentPredicate predicate) {
        this(color, predicate, GrantScope.TARGET);
    }

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, GrantScope scope) {
        this(color, null, scope);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetCategory.CREATURE, predicate);
            case SELF -> new TargetSpec(TargetCategory.NONE, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
