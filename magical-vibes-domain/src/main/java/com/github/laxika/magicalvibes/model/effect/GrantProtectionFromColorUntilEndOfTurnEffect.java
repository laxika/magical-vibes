package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Grants the target creature protection from a fixed, printed colour until end of turn (Willow
 * Priestess' "target green creature gains protection from black until end of turn").
 *
 * <p>Unlike {@link GrantProtectionChoiceUntilEndOfTurnEffect} the colour is known at build time, so
 * no player choice is prompted; the grant is stored in
 * {@code Permanent.protectionFromColorsUntilEndOfTurn} and cleared by the turn cleanup.
 *
 * @param color     the colour the target gains protection from
 * @param predicate an optional narrowing predicate on the legal target (e.g. "green creature");
 *                  {@code null} means any creature is legal
 */
public record GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color, PermanentPredicate predicate)
        implements CardEffect {

    public GrantProtectionFromColorUntilEndOfTurnEffect(CardColor color) {
        this(color, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, predicate);
    }
}
