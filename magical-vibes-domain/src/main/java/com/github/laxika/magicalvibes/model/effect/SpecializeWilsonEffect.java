package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Transforms Wilson, Bear Comrade into the selected digital specialized face. */
public record SpecializeWilsonEffect(CardColor color) implements CardEffect {
}
