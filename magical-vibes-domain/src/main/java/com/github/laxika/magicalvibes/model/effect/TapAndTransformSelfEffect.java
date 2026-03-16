package com.github.laxika.magicalvibes.model.effect;

/**
 * Taps the source permanent and then transforms it.
 * <p>
 * Used by Homicidal Brute (ISD back face): "At the beginning of your end step,
 * if Homicidal Brute didn't attack this turn, tap Homicidal Brute, then transform it."
 */
public record TapAndTransformSelfEffect() implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
