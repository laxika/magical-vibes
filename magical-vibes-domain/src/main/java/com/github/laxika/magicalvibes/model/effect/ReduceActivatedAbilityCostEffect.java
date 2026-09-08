package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}. The ordinary form applies to all matching abilities symmetrically; the
 * {@code powerUpOnly} form is controller-scoped and applies only to Power-up abilities.
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount, boolean appliesSymmetrically, boolean powerUpOnly)
        implements ActivatedAbilityCostReducingEffect {

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount) {
        this(predicate, amount, true, false);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, new Fixed(amount));
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount, boolean appliesSymmetrically) {
        this(predicate, amount, appliesSymmetrically, false);
    }

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount, boolean powerUpOnly) {
        this(predicate, new Fixed(amount), !powerUpOnly, powerUpOnly);
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

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return !powerUpOnly || ability.isPowerUpAbility();
    }

    @Override
    public boolean appliesSymmetrically() {
        return appliesSymmetrically && !powerUpOnly;
    }
}
