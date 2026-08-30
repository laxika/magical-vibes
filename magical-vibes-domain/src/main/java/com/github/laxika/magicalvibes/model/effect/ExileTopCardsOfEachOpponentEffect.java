package com.github.laxika.magicalvibes.model.effect;

/** Exiles the top {@code count} cards of each opponent's library. */
public record ExileTopCardsOfEachOpponentEffect(int count) implements CardEffect {
}
