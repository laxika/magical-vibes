package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Each opponent chooses a creature to sacrifice. After all choices, the chosen creatures are
 * sacrificed together and the effect controller creates one token for each creature sacrificed.
 */
public record EachOpponentSacrificesCreatureCreateTokensEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    @Override
    public int tokenPower() {
        return tokenTemplate.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenTemplate.tokenToughness();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.tokenType();
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
    }
}
