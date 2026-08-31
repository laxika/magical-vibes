package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Creates the supplied token profile attached to the targeted creature. */
public record CreateTokenAttachedToTargetEffect(CreateTokenEffect token,
                                               PlayerRelation targetControllerRelation)
        implements CardEffect, TokenCreatingEffect {

    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token) {
        this(token, PlayerRelation.SELF);
    }

    public CreateTokenAttachedToTargetEffect {
        targetControllerRelation = targetControllerRelation == null
                ? PlayerRelation.SELF : targetControllerRelation;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
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
