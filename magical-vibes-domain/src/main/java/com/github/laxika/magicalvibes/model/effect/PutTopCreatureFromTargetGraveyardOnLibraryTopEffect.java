package com.github.laxika.magicalvibes.model.effect;

/**
 * If the top card of target player's graveyard is a creature card, put that card on top of that
 * player's library. Empty graveyard or non-creature top card resolves as a no-op.
 * <p>
 * Used by Guiding Spirit.
 */
public record PutTopCreatureFromTargetGraveyardOnLibraryTopEffect() implements CardEffect {
    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
