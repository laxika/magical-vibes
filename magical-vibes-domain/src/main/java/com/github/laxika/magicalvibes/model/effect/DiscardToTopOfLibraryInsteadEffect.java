package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect (Library of Leng): "If an effect causes you to discard a card, discard
 * it, but you may put it on top of your library instead of into your graveyard."
 *
 * <p>The redirect is a "may" the discarding player controls. We model it as always keeping the card
 * (putting it on top of the library), which is the beneficial choice the controller would take when
 * they want to retain the discarded card; the card is still discarded, so discard triggers still fire.
 * Read directly by {@code GraveyardService.discardCard}, not resolved via a handler.
 */
public record DiscardToTopOfLibraryInsteadEffect() implements CardEffect {
}
