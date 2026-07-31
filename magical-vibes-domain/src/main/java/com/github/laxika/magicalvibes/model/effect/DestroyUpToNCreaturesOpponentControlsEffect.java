package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses up to {@code maxCount} creatures an opponent controls and destroys them;
 * if {@code cannotBeRegenerated} they can't be regenerated. Modeled as a resolution-time
 * multi-select rather than a cast-time target, so it can ride on resolution paths that have no
 * targeting pipeline — Fatal Lore, whose mode is only known once an opponent has chosen it.
 *
 * @param maxCount            the largest number of creatures that may be destroyed
 * @param cannotBeRegenerated whether regeneration shields are ignored
 */
public record DestroyUpToNCreaturesOpponentControlsEffect(int maxCount, boolean cannotBeRegenerated)
        implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return null;
    }
}
