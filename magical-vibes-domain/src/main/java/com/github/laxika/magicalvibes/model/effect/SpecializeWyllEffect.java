package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Wyll, Pact-Bound Duelist's five digital specialized faces. */
public record SpecializeWyllEffect(CardColor color) implements CardEffect {
}
