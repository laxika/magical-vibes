package com.github.laxika.magicalvibes.model.effect;

/** Exiles the top cards of the controller's library and suspends each exiled nonland. */
public record ExileTopCardsAndSuspendNonlandsWithManaValueEffect(int count) implements CardEffect {

    public ExileTopCardsAndSuspendNonlandsWithManaValueEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
    }
}
