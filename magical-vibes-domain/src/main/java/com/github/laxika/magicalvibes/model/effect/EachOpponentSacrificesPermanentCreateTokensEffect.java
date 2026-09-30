package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each opponent chooses a matching permanent to sacrifice. After all choices, the chosen
 * permanents are sacrificed together and the effect controller creates one token for each one.
 */
public record EachOpponentSacrificesPermanentCreateTokensEffect(
        PermanentPredicate sacrificeFilter,
        CreateTokenEffect tokenTemplate) implements TokenCreatingEffect {

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
