package com.github.laxika.magicalvibes.model.effect;

/** Looks at the top cards of the controller's library, then exiles one face down with a hatching counter. */
public record ExileOneFromTopCardsFaceDownWithHatchingCounterEffect(int count) implements CardEffect {
}
