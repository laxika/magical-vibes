package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts target permanent into its owner's library at a specific position from the top.
 * Position is 0-indexed: 0 = top, 1 = second from top, 2 = third from top, etc.
 *
 * @param position the dynamic 0-indexed position from the top of the library
 * @param self whether the source permanent is put into its owner's library instead of targeting a permanent
 */
public record PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position, boolean self) implements CardEffect {

    public PutTargetPermanentIntoLibraryNFromTopEffect(DynamicAmount position) {
        this(position, false);
    }

    public PutTargetPermanentIntoLibraryNFromTopEffect(int position) {
        this(new Fixed(position), false);
    }

    public static PutTargetPermanentIntoLibraryNFromTopEffect self(int position) {
        return new PutTargetPermanentIntoLibraryNFromTopEffect(new Fixed(position), true);
    }

    @Override
    public TargetSpec targetSpec() {
        return self ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent());
    }
}
