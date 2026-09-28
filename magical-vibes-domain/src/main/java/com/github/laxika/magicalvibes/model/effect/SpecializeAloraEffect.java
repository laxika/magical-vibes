package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Alora, Rogue Companion's five specialized faces. */
public record SpecializeAloraEffect(CardColor color) implements CardEffect {
}
