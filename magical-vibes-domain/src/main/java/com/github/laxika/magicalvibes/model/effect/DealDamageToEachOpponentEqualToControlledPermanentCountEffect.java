package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Deals damage to each opponent equal to the number of matching permanents that opponent controls.
 * The amount is evaluated separately for each opponent.
 */
public record DealDamageToEachOpponentEqualToControlledPermanentCountEffect(PermanentPredicate filter)
        implements CardEffect {
}
