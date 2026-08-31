package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/** Creates one token attached to each creature controlled by an opponent. */
public record CreateTokensAttachedToOpposingCreaturesEffect(CreateTokenEffect token)
        implements CardEffect, TokenCreatingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS);
    }

    @Override
    public CardType tokenType() {
        return token.primaryType();
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
