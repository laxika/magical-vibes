package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts target permanent into its owner's library at a specific position from the top.
 * Position is 0-indexed: 0 = top, 1 = second from top, 2 = third from top, etc.
 *
 * @param position the dynamic 0-indexed position from the top of the library
 */
public record PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position) implements CardEffect {

    public PutTargetPermanentIntoLibraryNFromTopEffect(int position) {
        this(new Fixed(position));
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
