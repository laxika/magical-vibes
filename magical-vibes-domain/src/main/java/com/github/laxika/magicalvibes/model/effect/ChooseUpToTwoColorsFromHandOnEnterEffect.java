package com.github.laxika.magicalvibes.model.effect;

/** "As this enters, choose up to two colors from among cards in your hand." */
public record ChooseUpToTwoColorsFromHandOnEnterEffect() implements ChooseColorEffect {

    @Override
    public int choicesRequired() {
        return 0;
    }

    @Override
    public int choicesMax() {
        return 2;
    }

    @Override
    public boolean colorsFromControllerHand() {
        return true;
    }
}
