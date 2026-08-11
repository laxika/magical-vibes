package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: the controller gains additional life whenever they would gain life.
 */
public record AdditionalLifeGainEffect(int amount) implements LifeGainReplacementEffect {

    @Override
    public int additionalLifeGain() {
        return amount;
    }
}
