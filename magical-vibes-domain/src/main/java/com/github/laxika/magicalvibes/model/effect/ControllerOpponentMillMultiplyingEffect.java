package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for static effects that multiply mill events affecting an opponent of the
 * permanent's controller.
 */
public interface ControllerOpponentMillMultiplyingEffect extends CardEffect {

    int millMultiplier();
}
