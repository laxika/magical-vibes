package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Creates a token copy of the permanent that caused a sacrifice trigger. */
public record CreateTokenCopyOfSacrificedPermanentEffect(Card copiedCard)
        implements CardEffect, SacrificedPermanentCardAwareEffect {

    public CreateTokenCopyOfSacrificedPermanentEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new CreateTokenCopyOfSacrificedPermanentEffect(sacrificedCard);
    }
}
