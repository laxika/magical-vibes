package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * For each opponent, the source's controller creates the wrapped token unless that opponent
 * sacrifices a creature of their choice.
 */
public record EachOpponentCreatesTokenUnlessSacrificesCreatureEffect(CreateTokenEffect token)
        implements TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return token.tokenAmount();
    }

    @Override
    public CardType tokenType() {
        return token.tokenType();
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
