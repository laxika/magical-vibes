package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/** Capability for sacrifice-triggered effects that need the sacrificed permanent's card snapshot. */
public interface SacrificedPermanentCardAwareEffect {

    /** Returns a copy of this effect with the sacrificed permanent's card bound in. */
    CardEffect boundToSacrificedPermanent(Card sacrificedCard);
}
