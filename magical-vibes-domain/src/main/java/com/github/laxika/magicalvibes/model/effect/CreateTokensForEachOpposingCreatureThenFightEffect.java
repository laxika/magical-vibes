package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Objects;

/** Creates one token for each opposing creature, then has the tokens fight distinct opposing creatures. */
public record CreateTokensForEachOpposingCreatureThenFightEffect(CreateTokenEffect tokenTemplate)
        implements TokenCreatingEffect {

    public CreateTokensForEachOpposingCreatureThenFightEffect {
        Objects.requireNonNull(tokenTemplate, "tokenTemplate");
    }

    @Override
    public DynamicAmount tokenAmount() {
        return new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS);
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
