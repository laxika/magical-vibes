package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for a static replacement effect that adds cards to mill events affecting an
 * opponent of the permanent's controller.
 */
public interface ControllerOpponentMillBonusEffect extends CardEffect {

    int amount();
}
