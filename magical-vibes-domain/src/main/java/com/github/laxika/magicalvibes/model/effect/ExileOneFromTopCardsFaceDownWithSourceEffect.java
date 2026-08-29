package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top cards of the controller's library, exile one face down tracked with the
 * source permanent, and put the rest on the bottom of that library in a random order.
 */
public record ExileOneFromTopCardsFaceDownWithSourceEffect(int count) implements CardEffect {
}
