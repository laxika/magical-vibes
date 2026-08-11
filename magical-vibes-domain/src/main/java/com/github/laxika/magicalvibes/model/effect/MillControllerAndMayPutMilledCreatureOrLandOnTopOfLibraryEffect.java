package com.github.laxika.magicalvibes.model.effect;

/**
 * Mills cards from the controller's library, then offers each creature or land card milled by
 * this resolution for placement on top of that library. The offers are represented by
 * {@link PutMilledCreatureOrLandOnTopOfLibraryEffect} marker effects.
 */
public record MillControllerAndMayPutMilledCreatureOrLandOnTopOfLibraryEffect(int count)
        implements CardEffect {
}
