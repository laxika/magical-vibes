package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Mills cards from the controller's library, then offers each matching card milled by this
 * resolution for return to its owner's hand, up to {@code maxCount} cards. The offers are represented by
 * {@link ReturnMilledPermanentToHandEffect} marker effects.
 */
public record MillControllerAndMayReturnMilledPermanentToHandEffect(
        DynamicAmount count, CardPredicate filter, int maxCount,
        CardPredicate bonusFilter, int bonusLife)
        implements CombatDamageAmountAwareEffect, CombatDamageTriggerContextEffect {

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count, CardPredicate filter) {
        this(new Fixed(count), filter, 1, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(DynamicAmount count, CardPredicate filter) {
        this(count, filter, 1, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(
            int count, CardPredicate filter, int maxCount, CardPredicate bonusFilter, int bonusLife) {
        this(new Fixed(count), filter, maxCount, bonusFilter, bonusLife);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count) {
        this(new Fixed(count), new CardIsPermanentPredicate(), 1, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(DynamicAmount count) {
        this(count, new CardIsPermanentPredicate(), 1, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(int count, int maxCount) {
        this(new Fixed(count), new CardIsPermanentPredicate(), maxCount, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(
            int count, CardPredicate filter, CardPredicate bonusFilter, int bonusLife) {
        this(new Fixed(count), filter, 1, bonusFilter, bonusLife);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(
            DynamicAmount count, CardPredicate filter, CardPredicate bonusFilter, int bonusLife) {
        this(count, filter, 1, bonusFilter, bonusLife);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(
            int count, CardPredicate filter, int maxCount) {
        this(new Fixed(count), filter, maxCount, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect(
            DynamicAmount count, CardPredicate filter, int maxCount) {
        this(count, filter, maxCount, null, 0);
    }

    public MillControllerAndMayReturnMilledPermanentToHandEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return count;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.SOURCE_SELF;
    }
}
