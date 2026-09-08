package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller's maximum hand size is increased by the given amount.
 * Checked during cleanup when calculating discard requirements.
 */
public record IncreaseControllerMaxHandSizeEffect(int increase) implements ControllerMaxHandSizeEffect {

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return currentMax + increase;
    }
}
