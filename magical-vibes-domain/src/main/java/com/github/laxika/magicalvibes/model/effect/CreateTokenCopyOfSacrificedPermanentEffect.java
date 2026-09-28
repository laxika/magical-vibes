package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Creates copies of a sacrificed permanent, either from an activated cost or a bound trigger. */
public record CreateTokenCopyOfSacrificedPermanentEffect(Card copiedCard, int amount)
        implements CardEffect, SacrificedPermanentCardAwareEffect {

    public CreateTokenCopyOfSacrificedPermanentEffect() {
        this(null, 1);
    }

    public CreateTokenCopyOfSacrificedPermanentEffect(int amount) {
        this(null, amount);
    }

    public CreateTokenCopyOfSacrificedPermanentEffect(Card copiedCard) {
        this(copiedCard, 1);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new CreateTokenCopyOfSacrificedPermanentEffect(sacrificedCard, amount);
    }
}
