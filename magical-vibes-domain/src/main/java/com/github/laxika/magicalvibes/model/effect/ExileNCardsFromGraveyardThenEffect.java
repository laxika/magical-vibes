package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles exactly {@code count} cards from the controller's graveyard and creates a reflexive
 * follow-up trigger only when all cards were exiled successfully.
 *
 * @param count the exact number of cards to exile
 * @param thenEffect the payload of the reflexive trigger
 */
public record ExileNCardsFromGraveyardThenEffect(int count, CardEffect thenEffect) implements CardEffect {

    public ExileNCardsFromGraveyardThenEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("Exile count must be positive");
        }
        if (thenEffect == null) {
            throw new IllegalArgumentException("ExileNCardsFromGraveyardThenEffect requires a payload");
        }
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
