package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}, for all players (static, symmetric). E.g. Heartstone with a creature
 * predicate and amount 1. The reduction can optionally preserve a minimum total cost of one
 * mana.
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                               boolean appliesSymmetrically,
                                               boolean preventsReductionBelowOneMana, boolean powerUpOnly)
        implements ActivatedAbilityCostReducingEffect {

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount) {
        this(predicate, amount, true, false, false);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount,
                                            boolean appliesSymmetrically) {
        this(predicate, amount, appliesSymmetrically, false, false);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, new Fixed(amount));
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                            boolean appliesSymmetrically,
                                            boolean preventsReductionBelowOneMana) {
        this(predicate, new Fixed(amount), appliesSymmetrically, preventsReductionBelowOneMana, false);
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int genericCostReduction() {
        return amount instanceof Fixed fixed ? fixed.value() : 0;
    }

    @Override
    public DynamicAmount genericCostReductionAmount() {
        return amount instanceof Fixed ? null : amount;
    }
    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount, boolean powerUpOnly) {
        this(predicate, new Fixed(amount), !powerUpOnly, false, powerUpOnly);
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return !powerUpOnly || ability.isPowerUpAbility();
    }

    @Override
    public boolean appliesSymmetrically() {
        return appliesSymmetrically && !powerUpOnly;
    }
}
