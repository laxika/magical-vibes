package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: this creature can't block.
 */
public record CantBlockEffect() implements BlockingRestrictionEffect {

    @Override
    public boolean cantBlock() {
        return true;
    }
}
