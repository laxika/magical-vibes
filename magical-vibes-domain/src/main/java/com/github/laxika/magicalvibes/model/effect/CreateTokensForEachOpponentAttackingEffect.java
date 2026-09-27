package com.github.laxika.magicalvibes.model.effect;

/** Creates the supplied token batch for each opponent, requiring each creature token to attack that opponent this turn if able. */
public record CreateTokensForEachOpponentAttackingEffect(CreateTokenEffect token) implements CardEffect {
}
