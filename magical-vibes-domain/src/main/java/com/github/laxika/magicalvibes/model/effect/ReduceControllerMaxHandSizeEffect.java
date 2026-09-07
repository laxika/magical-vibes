package com.github.laxika.magicalvibes.model.effect;

/**
 * Reduces the controller's maximum hand size by the given amount.
 * When resolved by a spell or ability, the reduction lasts for the rest of the game.
 */
public record ReduceControllerMaxHandSizeEffect(int reduction) implements ControllerMaxHandSizeEffect {

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return currentMax - reduction;
    }
}
