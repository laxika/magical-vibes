package com.github.laxika.magicalvibes.model.effect;

/**
 * Deals damage to each opponent who controls more lands than the effect controller, equal to
 * that opponent's land surplus. The effect records the total damage actually dealt on the stack
 * entry for a following {@link com.github.laxika.magicalvibes.model.amount.EventValue} effect.
 */
public record DealDamageToEachOpponentEqualToLandDifferenceEffect() implements CardEffect {
}
