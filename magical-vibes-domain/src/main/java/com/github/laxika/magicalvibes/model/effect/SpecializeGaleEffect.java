package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Gale, Conduit of the Arcane's five digital specialized faces. */
public record SpecializeGaleEffect(CardColor color) implements CardEffect {
}
