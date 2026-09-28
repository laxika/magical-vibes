package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Creates tokens after choosing their name at resolution. */
public record CreateTokenWithChosenNameEffect(CreateTokenEffect tokenTemplate) implements TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
    }

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }
}
