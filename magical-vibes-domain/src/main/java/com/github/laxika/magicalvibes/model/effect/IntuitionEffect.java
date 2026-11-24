package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * "Search your library for three cards and reveal them" (Intuition), "search your library for exactly two cards not
 * named Burning-Rune Demon that have different names. If you do, reveal those cards" (Burning-Rune Demon): the
 * controller searches their own library through a {@code PendingInteraction.IntuitionSearchChoice} and reveals what
 * they find. The found cards stay in the library (CR 701.20b) and are the step's {@code StepKind.SEARCH} and
 * {@code StepKind.REVEAL} result, so
 * "an opponent chooses one of them", "put that card into your hand and the rest into your graveyard" and "then
 * shuffle" are later steps over {@code Subjects.revealedThisWay()} and {@code ShuffleLibraryEffect}. The step records
 * the search (CR 701.23a); a prevented search finds nothing; Aven Mindcensor limits it to the top cards; under
 * Opposition Agent the found cards are exiled instead of revealed, playable by the Agent's controller.
 *
 * <p>Searching for a quantity alone finds that many, as many as the library has if fewer (CR 701.23d); a search with
 * a {@code filter} or {@code differentNames} may find none (CR 701.23b), and "exactly" finds either none or
 * {@code count}.
 *
 * @param count          how many cards are found
 * @param filter         which cards may be found; {@code null} for any card
 * @param differentNames whether the cards found must have different names ("exactly two cards ... that have
 *                       different names")
 */
public record IntuitionEffect(int count, CardPredicate filter, boolean differentNames) implements CardEffect {

    public IntuitionEffect {
        if (count < 1) {
            throw new IllegalArgumentException("A search finds at least one card: " + count);
        }
    }

    /** "Search your library for [count] cards and reveal them". */
    public static IntuitionEffect cards(int count) {
        return new IntuitionEffect(count, null, false);
    }

    /** "Search your library for exactly [count] [filter] cards that have different names ... reveal those cards". */
    public static IntuitionEffect exactlyDifferentlyNamed(int count, CardPredicate filter) {
        return new IntuitionEffect(count, filter, true);
    }
}
