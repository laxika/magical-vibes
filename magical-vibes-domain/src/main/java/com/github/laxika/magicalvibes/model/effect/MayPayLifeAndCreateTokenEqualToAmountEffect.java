package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Offers the controller an optional payment of life equal to {@code amount}; if paid, they create
 * one token whose power and toughness are both that amount.
 */
public record MayPayLifeAndCreateTokenEqualToAmountEffect(
        DynamicAmount amount,
        CreateTokenEffect tokenEffect
) implements TokenCreatingEffect {

    public MayPayLifeAndCreateTokenEqualToAmountEffect {
        if (amount == null) {
            throw new IllegalArgumentException(
                    "MayPayLifeAndCreateTokenEqualToAmountEffect requires an amount");
        }
        if (tokenEffect == null) {
            throw new IllegalArgumentException(
                    "MayPayLifeAndCreateTokenEqualToAmountEffect requires a token effect");
        }
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.tokenType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenToughness();
    }
}
