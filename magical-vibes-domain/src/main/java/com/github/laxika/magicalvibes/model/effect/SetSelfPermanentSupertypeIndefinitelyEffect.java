package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/**
 * Gives or removes a supertype from the source permanent indefinitely.
 */
public record SetSelfPermanentSupertypeIndefinitelyEffect(CardSupertype supertype, boolean gained)
        implements CardEffect {
}
