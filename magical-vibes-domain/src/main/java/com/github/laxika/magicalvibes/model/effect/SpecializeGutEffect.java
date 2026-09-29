package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Gut, Fanatical Priestess's five digital specialized faces. */
public record SpecializeGutEffect(CardColor color) implements CardEffect {
}
