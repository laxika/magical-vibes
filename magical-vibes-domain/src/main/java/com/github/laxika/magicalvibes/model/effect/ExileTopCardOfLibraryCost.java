package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect that exiles the top N cards of the controller's library as part of an activated
 * ability's cost. Unlike {@link ExileTopCardOfOwnLibraryEffect}, this is paid during activation
 * (before the ability hits the stack) and the ability cannot be activated if the controller's
 * library has fewer cards than required.
 *
 * @param imprintOnSource when {@code true}, the last card exiled this way is imprinted on the
 *                        source permanent so the ability's effect can look at it at resolution
 *                        (e.g. Storm Elemental's "if the exiled card is a snow land"), read back
 *                        through the {@code ImprintedCardMatches} condition.
 */
public record ExileTopCardOfLibraryCost(int count, boolean imprintOnSource) implements CostEffect {

    /** Plain exile cost that does not remember the exiled card. */
    public ExileTopCardOfLibraryCost(int count) {
        this(count, false);
    }
}
