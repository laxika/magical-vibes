package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Internal pending-choice marker for a normal-cost spell cast from exile. */
public record MayCastExiledCardWithNormalCostEffect(UUID offerGroupId,
                                                    boolean putOnBottomOfOwnersLibraryInsteadOfGraveyard)
        implements CardEffect {

    /** Creates the source-linked variant, which returns the spell to the bottom of its owner's library. */
    public MayCastExiledCardWithNormalCostEffect(UUID offerGroupId) {
        this(offerGroupId, true);
    }
}
