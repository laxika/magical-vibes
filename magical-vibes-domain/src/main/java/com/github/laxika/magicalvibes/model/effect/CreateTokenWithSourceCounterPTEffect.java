package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Creates a token whose characteristic-defining power and toughness equal the number of counters
 * on the permanent that created it. The engine binds that permanent's id when this effect resolves.
 */
public record CreateTokenWithSourceCounterPTEffect(
        CounterType counterType,
        CreateTokenEffect tokenTemplate
) implements CardEffect, TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return tokenTemplate.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenTemplate.primaryType();
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
