package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the top {@code count} cards of the controller's library, then has the controller choose
 * one instant or sorcery card exiled this way, copies it {@code copies} times and offers the
 * copies to be cast without paying their mana costs (Chandra, Pyromaster's ultimate).
 *
 * <p>The choice is made through {@code PendingInteraction.ExiledSpellCopyChoice}; the copies are
 * queued through the shared free-cast queue, so they are cast one at a time (each pausing for its
 * own targets) and cease to exist afterwards per CR 707.10a.
 *
 * @param count  how many cards are exiled from the top of the library
 * @param copies how many copies of the chosen card are created
 */
public record ExileTopCardsChooseSpellAndCastCopiesEffect(int count, int copies) implements CardEffect {

    public ExileTopCardsChooseSpellAndCastCopiesEffect {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive");
        }
        if (copies <= 0) {
            throw new IllegalArgumentException("copies must be positive");
        }
    }
}
