package com.github.laxika.magicalvibes.model.effect;

/** Capability marker for an effect that instructs a player to double something. */
public interface DoublingEffect extends CardEffect {

    /**
     * Whether this particular effect uses doubling language rather than a different multiplier.
     * Parameterized multiplier effects such as Nyxbloom Ancient override this to return false.
     */
    default boolean isDoublingEffect() {
        return true;
    }
}
