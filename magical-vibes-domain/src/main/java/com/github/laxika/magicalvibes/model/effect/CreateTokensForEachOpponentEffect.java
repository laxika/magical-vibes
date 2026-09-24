package com.github.laxika.magicalvibes.model.effect;

/** Creates the supplied token batch under the controller's control once for each opponent. */
public record CreateTokensForEachOpponentEffect(CreateTokenEffect token) implements CardEffect {
}
