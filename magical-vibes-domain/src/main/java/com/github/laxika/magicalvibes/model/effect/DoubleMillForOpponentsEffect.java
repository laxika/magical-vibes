package com.github.laxika.magicalvibes.model.effect;

/** Static replacement effect that doubles cards milled by each opponent of the source's controller. */
public record DoubleMillForOpponentsEffect() implements ControllerOpponentMillMultiplyingEffect {

    @Override
    public int millMultiplier() {
        return 2;
    }
}
