package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Exiles the sacrificed permanent card and tracks it with the source permanent. */
public record ExileSacrificedPermanentWithSourceEffect(Card sacrificedCard)
        implements CardEffect, SacrificedPermanentCardAwareEffect {

    public ExileSacrificedPermanentWithSourceEffect() {
        this(null);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new ExileSacrificedPermanentWithSourceEffect(sacrificedCard);
    }
}
