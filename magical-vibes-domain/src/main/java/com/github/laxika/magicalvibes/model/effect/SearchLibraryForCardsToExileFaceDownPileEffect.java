package com.github.laxika.magicalvibes.model.effect;

/**
 * ON_ENTER_BATTLEFIELD: the controller searches their library for {@code count} cards, exiles them
 * face down as a pile tracked "with" the source permanent, shuffles that pile, then shuffles their
 * library.
 *
 * <p>The search is a repeated single-card pick over the whole library (fail-to-find allowed per
 * CR 701.23b) driven by
 * {@code com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService} via the
 * {@code LibrarySearchDestination.EXILE_FACE_DOWN_PILE} destination, which shuffles the resulting
 * pile once the last pick is made. Pair with {@link RegisterNextDrawFromExiledPileReplacementEffect}
 * to draw from the pile. Used by Mangara's Tome.
 */
public record SearchLibraryForCardsToExileFaceDownPileEffect(int count) implements CardEffect {
}
