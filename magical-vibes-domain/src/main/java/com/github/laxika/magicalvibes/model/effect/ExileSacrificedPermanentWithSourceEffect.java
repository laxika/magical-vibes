package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Exiles the sacrificed permanent card and tracks it with the source permanent. When
 * {@code counterIfToken} is set and the sacrificed permanent was a token, the source permanent also gets one
 * counter of that type ("If that creature was a token, put a +1/+1 counter on this creature"), as part of the
 * same triggered ability.
 */
public record ExileSacrificedPermanentWithSourceEffect(Card sacrificedCard, CounterType counterIfToken)
        implements CardEffect, SacrificedPermanentCardAwareEffect {

    public ExileSacrificedPermanentWithSourceEffect() {
        this(null, null);
    }

    public ExileSacrificedPermanentWithSourceEffect(CounterType counterIfToken) {
        this(null, counterIfToken);
    }

    @Override
    public CardEffect boundToSacrificedPermanent(Card sacrificedCard) {
        return new ExileSacrificedPermanentWithSourceEffect(sacrificedCard, counterIfToken);
    }
}
