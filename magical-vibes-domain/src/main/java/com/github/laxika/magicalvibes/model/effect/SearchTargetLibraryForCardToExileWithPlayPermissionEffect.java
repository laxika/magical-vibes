package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches target opponent's library for any card, exiles it face down,
 * shuffles that player's library, and grants the caster permission to play
 * the exiled card for as long as it remains exiled.
 */
public record SearchTargetLibraryForCardToExileWithPlayPermissionEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
