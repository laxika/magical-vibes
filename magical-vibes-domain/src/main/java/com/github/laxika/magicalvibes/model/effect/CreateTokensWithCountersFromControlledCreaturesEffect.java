package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Creates the token template, then puts one counter of each kind found on creatures controlled by
 * the effect's controller onto either of the created tokens.
 */
public record CreateTokensWithCountersFromControlledCreaturesEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public CreateTokensWithCountersFromControlledCreaturesEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
    }

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
