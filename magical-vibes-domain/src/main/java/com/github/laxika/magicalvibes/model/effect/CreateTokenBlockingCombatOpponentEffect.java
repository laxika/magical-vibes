package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/** Creates the wrapped token and puts it onto the battlefield blocking the combat opponent. */
public record CreateTokenBlockingCombatOpponentEffect(CreateTokenEffect token)
        implements TokenCreatingEffect, CombatOpponentReferencingEffect {

    @Override
    public DynamicAmount tokenAmount() {
        return token.amount();
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
