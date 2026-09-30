package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect that shuffles a creature into its owner's library instead of letting it die.
 */
public record ShuffleIntoLibraryInsteadOfDyingEffect() implements DyingCreatureLibraryReplacementEffect {

    @Override
    public boolean putOnBottom() {
        return false;
    }

    @Override
    public boolean shuffleIntoLibrary() {
        return true;
    }
}
