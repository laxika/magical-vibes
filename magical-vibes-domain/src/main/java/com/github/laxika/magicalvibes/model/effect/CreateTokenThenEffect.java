package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the wrapped tokens and, if at least one token was created, puts the follow-up effect on
 * the stack as a reflexive triggered ability.
 */
public record CreateTokenThenEffect(CreateTokenEffect tokenEffect, CardEffect thenEffect)
        implements TokenCreatingEffect {

    public CreateTokenThenEffect {
        if (tokenEffect == null || thenEffect == null) {
            throw new IllegalArgumentException("CreateTokenThenEffect requires both effects");
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
