package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Reduces the generic mana portion of non-mana activated abilities activated by the controller of
 * this effect by {@code amount}.
 */
public record ReduceNonManaActivatedAbilityCostEffect(int amount)
        implements ActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate ALL_PERMANENTS = new PermanentTruePredicate();

    @Override
    public PermanentPredicate affectedPermanents() {
        return ALL_PERMANENTS;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return !ability.isManaAbility();
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }

    @Override
    public boolean preventsReductionBelowOneMana() {
        return true;
    }
}
