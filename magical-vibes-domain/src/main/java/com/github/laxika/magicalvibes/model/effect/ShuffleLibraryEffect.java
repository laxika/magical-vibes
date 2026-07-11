package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles a library.
 * When {@code targetPlayer} is false, shuffles the controller's library (e.g. Ponder "You may shuffle").
 * When {@code targetPlayer} is true, the effect targets a player who shuffles their library (e.g. Boggart Forager).
 */
public record ShuffleLibraryEffect(boolean targetPlayer) implements CardEffect {
    @Override public boolean canTargetPlayer() { return targetPlayer; }
}
