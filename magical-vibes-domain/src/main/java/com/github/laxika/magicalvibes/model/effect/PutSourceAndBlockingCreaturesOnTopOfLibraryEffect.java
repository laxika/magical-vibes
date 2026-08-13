package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the source permanent and each creature it is blocking on top of their owners' libraries,
 * optionally also including creatures blocking the source, then shuffles each affected owner's
 * library.
 */
public record PutSourceAndBlockingCreaturesOnTopOfLibraryEffect(boolean includeCreaturesBlockingSource)
        implements CardEffect {

    /** Gomazoa's one-directional version: the source and creatures it is blocking. */
    public PutSourceAndBlockingCreaturesOnTopOfLibraryEffect() {
        this(false);
    }
}
