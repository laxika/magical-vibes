package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches target player's library for up to {@code count} cards and exiles them,
 * then that player shuffles. Targets a player. Used by Jester's Cap.
 */
public record SearchTargetLibraryForCardsToExileEffect(int count) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
