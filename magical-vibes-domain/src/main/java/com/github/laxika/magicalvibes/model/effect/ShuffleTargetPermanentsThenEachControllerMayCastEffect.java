package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Shuffles each selected permanent into its owner's library, then lets each affected controller
 * exile cards until a nonland card and cast that card without paying its mana cost.
 */
public record ShuffleTargetPermanentsThenEachControllerMayCastEffect(
        PermanentPredicate targetFilter
) implements CardEffect {

    public ShuffleTargetPermanentsThenEachControllerMayCastEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.benign(TargetPredicates.permanent())
                : TargetSpec.benign(TargetPredicates.permanent(), targetFilter);
    }
}
