package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates one token for each owner of cards still exiled with the source permanent, with the
 * token's power and toughness equal to the summed mana values of that owner's cards. The source is the stack
 * entry's source permanent, which a "when this leaves the battlefield" ability's entry keeps after the source is gone.
 */
public record CreateTokensForExiledCardsWithSourceEffect(CreateTokenEffect tokenEffect)
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
