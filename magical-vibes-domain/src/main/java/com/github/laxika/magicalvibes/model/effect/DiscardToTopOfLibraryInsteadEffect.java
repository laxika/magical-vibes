package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect for redirecting a discard to the top of its owner's library.
 *
 * <p>The redirect is a "may" the discarding player controls. We model it as always keeping the card
 * (putting it on top of the library), which is the beneficial choice the controller would take when
 * they want to retain the discarded card; the card is still discarded, so discard triggers still fire.
 * Read directly by {@code GraveyardService.discardCard}, not resolved via a handler.
 *
 * @param opponentCausedOnly whether the replacement applies only when an opponent caused the
 *                           discard (Nephalia Academy); {@code false} is Library of Leng's mode
 */
public record DiscardToTopOfLibraryInsteadEffect(boolean opponentCausedOnly) implements CardEffect {

    public DiscardToTopOfLibraryInsteadEffect() {
        this(false);
    }
}
