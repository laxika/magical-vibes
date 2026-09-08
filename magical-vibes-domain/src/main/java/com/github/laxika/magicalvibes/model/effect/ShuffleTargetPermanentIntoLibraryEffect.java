package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Target permanent's owner shuffles it into their library, optionally followed by an existing
 * effect resolved for the target permanent's controller or owner.
 * Used by Deglamer / Unravel the Aether style effects and Oblation.
 */
public record ShuffleTargetPermanentIntoLibraryEffect(
        PermanentPredicate predicate,
        CardEffect thenEffect,
        ThenEffectRecipient recipient
) implements CardEffect {

    public ShuffleTargetPermanentIntoLibraryEffect() {
        this(null, null, null);
    }

    public ShuffleTargetPermanentIntoLibraryEffect(PermanentPredicate predicate) {
        this(predicate, null, null);
    }

    public ShuffleTargetPermanentIntoLibraryEffect(CardEffect thenEffect, ThenEffectRecipient recipient) {
        this(null, thenEffect, recipient);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), predicate);
    }
}
