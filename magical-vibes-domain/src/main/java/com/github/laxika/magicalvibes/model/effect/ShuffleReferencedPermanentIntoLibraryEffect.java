package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles a permanent named by a {@link PermanentReference} into its owner's library.
 * The default reference is the permanent attached to the source Aura or Equipment.
 */
public record ShuffleReferencedPermanentIntoLibraryEffect(PermanentReference reference)
        implements CardEffect {

    public ShuffleReferencedPermanentIntoLibraryEffect() {
        this(PermanentReference.ATTACHED);
    }
}
