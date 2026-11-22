package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;

/**
 * Creates the supplied token profile attached to a permanent named by {@link #subject()} that matches
 * {@link #targetFilter()} and is controlled by {@link #targetControllerRelation()}:
 * <ul>
 *   <li>{@code target()} (the default): each target ("create a Wicked Role token attached to target creature you
 *       control");</li>
 *   <li>{@code source()}: this creature ("create a Monster Role token attached to it" in its own enters ability);</li>
 *   <li>{@code triggering()}: the permanent the trigger event was about ("attached to that creature"), or, when the
 *       event was about several, one of them that still fits, chosen as the ability resolves without targeting
 *       ("create a Wicked Role token attached to one of them", Dunbarrow Revivalist's boon).</li>
 * </ul>
 */
public record CreateTokenAttachedToTargetEffect(CreateTokenEffect token,
                                               PlayerRelation targetControllerRelation,
                                               PermanentPredicate targetFilter,
                                               PermanentSubject subject)
        implements CardEffect, TokenCreatingEffect {

    /** The token attached to a target matching {@code targetFilter} controlled by {@code targetControllerRelation}. */
    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token, PlayerRelation targetControllerRelation,
                                             PermanentPredicate targetFilter) {
        this(token, targetControllerRelation, targetFilter, null);
    }

    /** {@code token} attached to the creature {@code subject} names, whoever controls it. */
    public CreateTokenAttachedToTargetEffect(CreateTokenEffect token, PermanentSubject subject) {
        this(token, PlayerRelation.ANY, new PermanentIsCreaturePredicate(), subject);
    }

    /** {@code token} attached to one of the creatures whose event made the ability trigger, chosen as it resolves. */
    public static CreateTokenAttachedToTargetEffect attachedToOneOfTriggeringCreatures(CreateTokenEffect token) {
        return new CreateTokenAttachedToTargetEffect(token, Subjects.triggering());
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
        subject = subject == null ? Subjects.target() : subject;
        if (!(subject instanceof PermanentSubject.Target) && !(subject instanceof PermanentSubject.Source)
                && !(subject instanceof PermanentSubject.Triggering)
                && !(subject instanceof PermanentSubject.Returned)
                && !(subject instanceof PermanentSubject.ReturnedThisResolution)) {
            throw new IllegalArgumentException("A token is attached to target(), source(), triggering(), returned() or "
                    + "returnedThisResolution(): " + subject);
        }
    }

    @Override
    public TargetSpec targetSpec() {
        if (!subject.declaresTarget()) {
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
