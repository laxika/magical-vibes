package com.github.laxika.magicalvibes.model.effect;

/**
 * Target permanent's owner shuffles it into their library.
 * Used by Deglamer / Unravel the Aether style effects.
 */
public record ShuffleTargetPermanentIntoLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
