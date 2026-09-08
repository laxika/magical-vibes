package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Puts target permanent into its owner's library at a position from the top, then resolves a
 * follow-up effect using a snapshot of the removed permanent's controller or owner.
 */
public record PutTargetPermanentIntoLibraryNFromTopThenEffect(
        DynamicAmount position,
        CardEffect thenEffect,
        ThenEffectRecipient recipient
) implements CardEffect {

    public PutTargetPermanentIntoLibraryNFromTopThenEffect(
            int position, CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(new Fixed(position), thenEffect, recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
