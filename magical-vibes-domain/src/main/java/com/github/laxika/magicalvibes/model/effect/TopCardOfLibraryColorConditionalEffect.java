package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Conditional wrapper that applies the wrapped effect only while the top card of
 * the controller's library is the specified color. Used by Vampire Nocturnus and
 * similar cards that check the top card of the library for a condition.
 */
public record TopCardOfLibraryColorConditionalEffect(CardColor color, CardEffect wrapped) implements ConditionalEffect {

    @Override
    public String conditionName() {
        return "top card of library is " + color.name().toLowerCase();
    }

    @Override
    public String conditionNotMetReason() {
        return "top card of library is not " + color.name().toLowerCase();
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
