package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Creates the configured token(s), then attaches each created Equipment token to the source
 * permanent when the source is still on the battlefield and the attachment is legal.
 */
public record CreateTokenAndAttachToSourceEffect(CreateTokenEffect token)
        implements CardEffect, TokenCreatingEffect {

    public CreateTokenAndAttachToSourceEffect {
        Objects.requireNonNull(token, "token");
    }

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
    }

    @Override
    public int tokenPower() {
        return token.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return token.tokenToughness();
    }
}
