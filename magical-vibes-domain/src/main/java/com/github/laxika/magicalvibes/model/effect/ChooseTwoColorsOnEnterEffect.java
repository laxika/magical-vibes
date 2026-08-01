package com.github.laxika.magicalvibes.model.effect;

/**
 * An as-enters choice of two different colors, stored on the entering permanent.
 */
public record ChooseTwoColorsOnEnterEffect() implements ChooseColorEffect {

    @Override
    public int choicesRequired() {
        return 2;
    }
}
