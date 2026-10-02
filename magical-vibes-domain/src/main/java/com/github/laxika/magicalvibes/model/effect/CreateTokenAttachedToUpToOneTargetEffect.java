package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Creates the supplied token profile attached to zero or one target creature. */
public record CreateTokenAttachedToUpToOneTargetEffect(CreateTokenEffect token,
                                                       PlayerRelation targetControllerRelation,
                                                       PermanentPredicate targetFilter)
        implements CardEffect, TokenCreatingEffect, OptionalTargetEffect {

    public CreateTokenAttachedToUpToOneTargetEffect(CreateTokenEffect token) {
        this(token, PlayerRelation.SELF, new PermanentIsCreaturePredicate());
    }

    public CreateTokenAttachedToUpToOneTargetEffect {
        targetControllerRelation = targetControllerRelation == null
                ? PlayerRelation.SELF : targetControllerRelation;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetFilter);
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
