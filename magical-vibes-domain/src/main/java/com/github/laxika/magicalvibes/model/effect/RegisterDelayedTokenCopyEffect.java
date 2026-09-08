package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Registers a delayed trigger that creates a token copy of a sacrificed artifact. */
public record RegisterDelayedTokenCopyEffect(Card copiedCard)
        implements CardEffect, SacrificedPermanentCardAwareEffect {

    public RegisterDelayedTokenCopyEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new RegisterDelayedTokenCopyEffect(sacrificedCard);
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return true;
    }
}
