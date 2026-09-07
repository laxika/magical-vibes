package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Puts a target permanent matching the filter into its owner's library at a position from the top,
 * then lets that owner choose whether it stays there or moves to the bottom instead.
 *
 * @param position the 0-indexed position from the top of the library
 * @param targetPredicate the permanent restriction for the target
 */
public record PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(int position,
                                                                   PermanentPredicate targetPredicate)
        implements CardEffect {

    public PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(int position) {
        this(position, new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), targetPredicate);
    }
}
