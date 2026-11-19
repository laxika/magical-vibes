package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Creates the supplied token profile attached to a target matching {@link #targetFilter()}, or, when
 * {@code oneOfTriggeringPermanents}, attached to one of the permanents whose event made the ability trigger, chosen as it
 * resolves without targeting ("create a Wicked Role token attached to one of them", Dunbarrow Revivalist's boon).
 */
public record CreateTokenAttachedToTargetEffect(CreateTokenEffect token,
                                               PlayerRelation targetControllerRelation,
                                               PermanentPredicate targetFilter,
                                               boolean oneOfTriggeringPermanents)
        implements CardEffect, TokenCreatingEffect {

    /** The token attached to a target matching {@code targetFilter} controlled by {@code targetControllerRelation}. */
    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token, PlayerRelation targetControllerRelation,
                                             PermanentPredicate targetFilter) {
        this(token, targetControllerRelation, targetFilter, false);
    }

    /** {@code token} attached to one of the creatures whose event made the ability trigger, chosen as it resolves. */
    public static CreateTokenAttachedToTargetEffect attachedToOneOfTriggeringCreatures(CreateTokenEffect token) {
        return new CreateTokenAttachedToTargetEffect(token, PlayerRelation.ANY, new PermanentIsCreaturePredicate(),
                true);
    }

    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token) {
        this(token, PlayerRelation.SELF, new PermanentIsCreaturePredicate());
    }

    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token, PlayerRelation targetControllerRelation) {
        this(token, targetControllerRelation, new PermanentIsCreaturePredicate());
    }

    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token, PermanentPredicate targetFilter) {
        this(token, PlayerRelation.SELF, targetFilter);
    }

    public CreateTokenAttachedToTargetEffect {
        targetControllerRelation = targetControllerRelation == null
                ? PlayerRelation.SELF : targetControllerRelation;
    }

    @Override
    public TargetSpec targetSpec() {
        if (oneOfTriggeringPermanents) {
            return TargetSpec.NONE;
        }
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
