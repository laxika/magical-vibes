package com.github.laxika.magicalvibes.model.effect;

/**
 * Repeatedly chooses a nonlegendary creature that saddled the source this turn and creates a
 * tapped and attacking token copy of it, sacrificing each copy at the next end step.
 */
public record CreateTokenCopiesOfSaddledCreatureEffect(int amount) implements CardEffect {

    public CreateTokenCopiesOfSaddledCreatureEffect {
        if (amount < 1) {
            throw new IllegalArgumentException("amount must be positive");
        }
    }
}
