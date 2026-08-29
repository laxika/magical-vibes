package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect that modifies life loss by an opponent of its controller.
 */
public interface OpponentLifeLossReplacementEffect extends CardEffect {

    int lifeLossMultiplier();
}
