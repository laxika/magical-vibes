package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}, for all players (static, symmetric). E.g. Heartstone with a creature
 * predicate and amount 1.
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, DynamicAmount amount)
        implements ActivatedAbilityCostReducingEffect {

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, new Fixed(amount));
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
}
