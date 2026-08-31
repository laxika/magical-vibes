package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Creates the supplied token profile attached to the permanent created by a preceding graveyard return. */
public record CreateTokenAttachedToReturnedPermanentEffect(CreateTokenEffect token,
                                                           PlayerRelation targetControllerRelation)
        implements CardEffect, TokenCreatingEffect {

    public CreateTokenAttachedToReturnedPermanentEffect(CreateTokenEffect token) {
        this(token, PlayerRelation.SELF);
    }

    public CreateTokenAttachedToReturnedPermanentEffect {
        targetControllerRelation = targetControllerRelation == null
                ? PlayerRelation.SELF : targetControllerRelation;
    }

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
