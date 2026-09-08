package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Reduces the generic mana portion of activated abilities of permanents matching {@code predicate}
 * by {@code amount}. The ordinary form applies to all matching abilities symmetrically; the
 * {@code powerUpOnly} form is controller-scoped and applies only to Power-up abilities.
 */
public record ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                               boolean powerUpOnly)
        implements ActivatedAbilityCostReducingEffect {

    public ReduceActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false);
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return !powerUpOnly || ability.isPowerUpAbility();
    }

    @Override
    public boolean appliesSymmetrically() {
        return !powerUpOnly;
    }
}
