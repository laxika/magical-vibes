package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/** Creates a token attached to the targeted creature, then queues a reflexive follow-up effect. */
public record CreateTokenAttachedToTargetThenEffect(
        CreateTokenEffect tokenEffect,
        CardEffect thenEffect,
        PlayerRelation targetControllerRelation
) implements TokenCreatingEffect {

    public CreateTokenAttachedToTargetThenEffect(CreateTokenEffect tokenEffect, CardEffect thenEffect) {
        this(tokenEffect, thenEffect, PlayerRelation.SELF);
    }

    public CreateTokenAttachedToTargetThenEffect {
        if (tokenEffect == null || thenEffect == null) {
            throw new IllegalArgumentException("CreateTokenAttachedToTargetThenEffect requires both effects");
        }
        targetControllerRelation = targetControllerRelation == null
                ? PlayerRelation.SELF : targetControllerRelation;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public DynamicAmount tokenAmount() {
        return tokenEffect.amount();
    }

    @Override
    public CardType tokenType() {
        return tokenEffect.primaryType();
    }

    @Override
    public int tokenPower() {
        return tokenEffect.tokenPower();
    }

    @Override
    public int tokenToughness() {
        return tokenEffect.tokenToughness();
    }
}
