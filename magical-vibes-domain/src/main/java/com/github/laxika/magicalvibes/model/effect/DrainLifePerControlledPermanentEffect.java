package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target player loses X life and controller gains X life,
 * where X = multiplier × number of permanents the controller controls matching the filter.
 * Used by Tezzeret, Agent of Bolas -4 and similar effects. When {@code gainsOnlyLifeLost} is
 * {@code true} the controller gains only the life the target actually lost ("you gain that much
 * life"), so nothing is gained when the target cannot lose life (Foul-Tongue Shriek).
 */
public record DrainLifePerControlledPermanentEffect(
        PermanentPredicate filter,
        int multiplier,
        boolean gainsOnlyLifeLost
) implements CardEffect {

    public DrainLifePerControlledPermanentEffect(PermanentPredicate filter, int multiplier) {
        this(filter, multiplier, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
