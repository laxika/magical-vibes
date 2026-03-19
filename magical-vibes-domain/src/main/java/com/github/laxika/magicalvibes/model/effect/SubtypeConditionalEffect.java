package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Conditional wrapper: the wrapped effect only fires if the triggering creature
 * has the specified {@code subtype}.
 * <p>
 * Works with any trigger slot where the triggering event involves a creature whose
 * subtypes can be checked — e.g. ON_ALLY_CREATURE_ENTERS_BATTLEFIELD (Champion of
 * the Parish) or ON_ANY_CREATURE_DIES (Village Cannibals).
 */
public record SubtypeConditionalEffect(
        CardSubtype subtype,
        CardEffect wrapped
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return wrapped.canTargetPermanent();
    }

    @Override
    public boolean canTargetPlayer() {
        return wrapped.canTargetPlayer();
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
