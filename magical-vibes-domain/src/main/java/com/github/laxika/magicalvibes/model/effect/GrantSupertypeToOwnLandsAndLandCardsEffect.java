package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/** Static effect that grants a supertype to controlled lands and owned land cards in the library. */
public record GrantSupertypeToOwnLandsAndLandCardsEffect(CardSupertype supertype)
        implements OwnLandSupertypeGrantingEffect {
}
