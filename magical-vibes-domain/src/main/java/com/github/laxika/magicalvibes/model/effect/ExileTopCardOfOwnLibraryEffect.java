package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: "Exile the top card of your library."
 * Used as part of activated abilities (e.g. Precognition Field's "{3}: Exile the
 * top card of your library.").
 *
 * @param trackWithSource when {@code true}, the exiled card is also tracked in
 *                        {@code permanentExiledCards} under the source permanent
 *                        (e.g. Rona, Disciple of Gix). When {@code false}, the card
 *                        is simply exiled to the player's exile zone.
 */
public record ExileTopCardOfOwnLibraryEffect(boolean trackWithSource) implements CardEffect {
}
