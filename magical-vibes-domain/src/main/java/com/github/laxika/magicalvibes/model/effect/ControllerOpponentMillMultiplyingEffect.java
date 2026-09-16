package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static replacement effect that multiplies mill events affecting an opponent
 * of the permanent's controller.
 */
public interface ControllerOpponentMillMultiplyingEffect extends CardEffect {

    int millMultiplier();
}
