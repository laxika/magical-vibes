package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Reduces the generic cost of exhaust abilities of other permanents controlled by this permanent's
 * controller.
 */
public record ReduceExhaustAbilityCostEffect(int amount) implements ActivatedAbilityCostReducingEffect {

    private static final PermanentPredicate OTHER_OWN_PERMANENTS = new PermanentAllOfPredicate(List.of(
            new PermanentControlledBySourceControllerPredicate(),
            new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
    ));

    @Override
    public PermanentPredicate affectedPermanents() {
        return OTHER_OWN_PERMANENTS;
    }

    @Override
    public int genericCostReduction() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability) {
        return ability.isExhaustAbility();
    }

    @Override
    public boolean appliesSymmetrically() {
        return false;
    }
}
