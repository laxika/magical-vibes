package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Vhal, Eager Scholar's five specialized faces. */
public record SpecializeVhalEffect(CardColor color) implements CardEffect {
}
