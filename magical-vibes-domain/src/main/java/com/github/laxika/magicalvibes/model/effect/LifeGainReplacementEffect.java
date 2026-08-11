package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement that modifies life-gain events for the controller.
 */
public interface LifeGainReplacementEffect extends CardEffect {

    default int additionalLifeGain() {
        return 0;
    }

    default int lifeGainMultiplier() {
        return 1;
    }
}
