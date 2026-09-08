package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect: if this creature would die, put it on the bottom of its owner's library
 * instead.
 */
public record PutOnBottomOfLibraryInsteadOfDyingEffect() implements DyingCreatureLibraryReplacementEffect {

    @Override
    public boolean putOnBottom() {
        return true;
    }
}
