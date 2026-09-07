package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As an additional cost to cast this spell, you may sacrifice any number of creatures.
 * This spell costs {@code reductionPerCreature} less to cast for each creature sacrificed this way.
 */
public record SacrificeCreaturesForCostReductionEffect(
        int reductionPerCreature, PermanentPredicate filter) implements CardEffect {

    public SacrificeCreaturesForCostReductionEffect(int reductionPerCreature) {
        this(reductionPerCreature, new PermanentIsCreaturePredicate());
    }
}
