package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates the token under the owner of the source's imprinted card. Its source-relative power and toughness are
 * evaluated as the ability resolves against the source, else the source as it last existed on the battlefield (the
 * stack entry's source snapshot, for "when this leaves the battlefield").
 */
public record CreateTokenForImprintedCardOwnerEffect(CreateTokenEffect tokenEffect)
        implements CardEffect, TokenCreatingEffect {

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
