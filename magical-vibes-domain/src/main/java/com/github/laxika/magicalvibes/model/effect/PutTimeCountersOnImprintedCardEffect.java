package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;

import java.util.UUID;

/** Adds time counters to the card exiled as an activation cost. */
public record PutTimeCountersOnImprintedCardEffect(DynamicAmount amount, UUID cardId)
    implements ActivationCostCardReferenceEffect {

    public PutTimeCountersOnImprintedCardEffect(int amount) {
        this(positiveFixed(amount), null);
    }

    public PutTimeCountersOnImprintedCardEffect(DynamicAmount amount) {
        this(amount, null);
    }

    public PutTimeCountersOnImprintedCardEffect {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
    }

    private static DynamicAmount positiveFixed(int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
        return new Fixed(amount);
    }

    @Override
    public CardEffect bindToCard(UUID cardId) {
        return new PutTimeCountersOnImprintedCardEffect(amount, cardId);
    }

    @Override
    public CardEffect bindToCard(UUID cardId, int paidCardManaValue) {
        DynamicAmount boundAmount = amount instanceof ImprintedCardManaValue
                ? new Fixed(paidCardManaValue) : amount;
        return new PutTimeCountersOnImprintedCardEffect(boundAmount, cardId);
    }
}
