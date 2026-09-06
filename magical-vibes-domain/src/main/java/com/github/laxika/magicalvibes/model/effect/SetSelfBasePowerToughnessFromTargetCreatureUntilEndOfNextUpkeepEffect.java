package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Sets the source creature's base power and toughness to the target creature's current power and
 * toughness through the end of the source controller's next upkeep.
 */
public record SetSelfBasePowerToughnessFromTargetCreatureUntilEndOfNextUpkeepEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()));
    }
}
