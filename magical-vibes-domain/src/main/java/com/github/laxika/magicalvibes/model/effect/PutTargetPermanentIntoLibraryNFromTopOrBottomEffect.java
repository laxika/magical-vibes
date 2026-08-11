package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Puts a target nonland permanent into its owner's library at a position from the top, then lets
 * that owner choose whether it stays there or moves to the bottom instead.
 *
 * @param position the 0-indexed position from the top of the library
 */
public record PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(int position) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentNotPredicate(new PermanentIsLandPredicate()));
    }
}
