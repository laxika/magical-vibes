package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * As an additional cost to cast this spell, you may sacrifice any number of matching permanents.
 * This spell costs {@code reductionPerCreature} less to cast for each permanent sacrificed this
 * way. The one-argument constructor retains the creature-only behavior used by Torgaar.
 */
public record SacrificeCreaturesForCostReductionEffect(int reductionPerCreature,
                                                        PermanentPredicate filter) implements CardEffect {

    public SacrificeCreaturesForCostReductionEffect(int reductionPerCreature) {
        this(reductionPerCreature, new PermanentIsCreaturePredicate());
    }
}
