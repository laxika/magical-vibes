package com.github.laxika.magicalvibes.model.effect;

/**
 * "As this permanent enters, choose a number between {@code minNumber} and {@code maxNumber}
 * at random." The chosen value is stored on the entering permanent.
 */
public record ChooseRandomNumberOnEnterEffect(int minNumber, int maxNumber)
        implements NumberChoiceEffect {

    @Override
    public boolean chooseRandomly() {
        return true;
    }
}
