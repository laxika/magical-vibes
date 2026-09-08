package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Adds time counters to the card exiled as an activation cost. */
public record PutTimeCountersOnImprintedCardEffect(int amount, UUID cardId)
        implements ActivationCostCardReferenceEffect {

    public PutTimeCountersOnImprintedCardEffect(int amount) {
        this(amount, null);
    }

    public PutTimeCountersOnImprintedCardEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }

    @Override
    public CardEffect bindToCard(UUID cardId) {
        return new PutTimeCountersOnImprintedCardEffect(amount, cardId);
    }
}
