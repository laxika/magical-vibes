package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target permanent's owner shuffles it into their library.
 * Used by Deglamer / Unravel the Aether style effects.
 */
public record ShuffleTargetPermanentIntoLibraryEffect(PermanentPredicate predicate) implements CardEffect {

    public ShuffleTargetPermanentIntoLibraryEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), predicate);
    }
}
