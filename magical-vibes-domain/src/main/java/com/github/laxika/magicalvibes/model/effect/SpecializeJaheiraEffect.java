package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/** Applies one of Jaheira, Harper Emissary's five specialized faces. */
public record SpecializeJaheiraEffect(CardColor color) implements CardEffect {
}
