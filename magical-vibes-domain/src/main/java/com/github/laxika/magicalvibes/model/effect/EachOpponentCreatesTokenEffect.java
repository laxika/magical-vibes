package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent of the ability's controller creates the described tokens under their own control.
 */
public record EachOpponentCreatesTokenEffect(CreateTokenEffect token) implements CardEffect {
}
