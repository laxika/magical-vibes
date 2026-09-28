package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Karlach, Raging Tiefling's digital specialized faces. */
public record SpecializeKarlachEffect(CardColor color) implements CardEffect {
}
