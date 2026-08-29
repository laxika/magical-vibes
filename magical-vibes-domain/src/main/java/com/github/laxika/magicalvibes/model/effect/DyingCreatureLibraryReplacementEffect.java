package com.github.laxika.magicalvibes.model.effect;

/**
 * Replacement effect that moves a creature to its owner's library instead of letting it die.
 */
public interface DyingCreatureLibraryReplacementEffect extends CardEffect {

    boolean putOnBottom();

    default boolean mayChoose() {
        return false;
    }
}
