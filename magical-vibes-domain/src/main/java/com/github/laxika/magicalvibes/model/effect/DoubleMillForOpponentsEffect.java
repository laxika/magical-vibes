package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that doubles mill events affecting an opponent of this permanent's
 * controller.
 */
public record DoubleMillForOpponentsEffect() implements ControllerOpponentMillMultiplyingEffect {

    @Override
    public int millMultiplier() {
        return 2;
    }
}
