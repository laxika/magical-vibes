package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top {@code count} cards of target player's library (they stay on top in the same
 * order — no reordering), then the controller may have that player shuffle their library.
 * Models Visions. The optional shuffle is offered as a may-ability wrapping
 * {@link ShuffleLibraryEffect} (targeting the same player), reusing the shared shuffle handler.
 */
public record LookAtTopCardsOfTargetLibraryMayShuffleEffect(int count) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
