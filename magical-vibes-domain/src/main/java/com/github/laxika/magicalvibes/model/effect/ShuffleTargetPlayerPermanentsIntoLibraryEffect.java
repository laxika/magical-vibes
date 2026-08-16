package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Shuffles each permanent matching {@code filter} that the target player controls into its
 * owner's library.
 */
public record ShuffleTargetPlayerPermanentsIntoLibraryEffect(PermanentPredicate filter)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
