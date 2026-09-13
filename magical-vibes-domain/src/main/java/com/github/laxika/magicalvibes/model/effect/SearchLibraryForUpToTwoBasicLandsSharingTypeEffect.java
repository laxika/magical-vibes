package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches for up to two basic lands that share a land type. The first pick is unrestricted among
 * basic lands; the second pick is constrained by the land types of the first pick.
 */
public record SearchLibraryForUpToTwoBasicLandsSharingTypeEffect(boolean secondPick)
        implements CardEffect {

    public SearchLibraryForUpToTwoBasicLandsSharingTypeEffect() {
        this(false);
    }
}
