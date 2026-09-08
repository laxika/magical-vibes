package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSupertype;

/** Static effect that grants a supertype to every nonland permanent. */
public record GrantSupertypeToAllNonlandPermanentsEffect(CardSupertype supertype) implements CardEffect {
}
