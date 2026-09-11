package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.UUID;

/**
 * Creates one token for each owner of cards still exiled with the source permanent, with the
 * token's power and toughness equal to the summed mana values of that owner's cards.
 */
public record CreateTokensForExiledCardsWithSourceEffect(CreateTokenEffect tokenEffect,
                                                         UUID sourcePermanentId)
        implements CardEffect, TokenCreatingEffect {

    public CreateTokensForExiledCardsWithSourceEffect(CreateTokenEffect tokenEffect) {
        this(tokenEffect, null);
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
