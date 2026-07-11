package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Search your library for up to {@code count} cards matching {@code filter} and move them to
 * {@code destination} (hand, battlefield, battlefield tapped, or top of library), then shuffle.
 *
 * <p>A {@code null} filter is an unrestricted search (any card): the cards are not revealed and the
 * search cannot fail to find. A non-null filter restricts the search: the chosen cards are revealed
 * (for {@code HAND}/{@code TOP_OF_LIBRARY} destinations) and the search may fail to find. The prompt
 * and log text are derived from the filter (via {@code CardPredicateUtils.describeFilter}).
 *
 * <p>{@code manaValueBound} adds an X-relative mana-value constraint (see {@link XManaValueBound});
 * {@code null} means none. {@code castFromGraveyardCount} overrides {@code count} when the producing
 * spell was cast from a graveyard (flashback) — see Increasing Ambition (1 normally, 2 from a
 * graveyard).
 *
 * <p>Replaced the {@code SearchLibraryFor*} family (to-hand tutors, by-name searches, to-top,
 * creature-to-battlefield with MV/colour/subtype constraints, and card-types-to-battlefield).
 */
public record SearchLibraryEffect(
        DynamicAmount count,
        CardPredicate filter,
        LibrarySearchDestination destination,
        XManaValueBound manaValueBound,
        int castFromGraveyardCount
) implements CardEffect {

    /** Unrestricted single-card tutor to hand (e.g. Diabolic Tutor). */
    public SearchLibraryEffect() {
        this(new Fixed(1), null, LibrarySearchDestination.HAND, null, 1);
    }

    /** Single card matching {@code filter} to hand (basic land, artifact, creature, …). */
    public SearchLibraryEffect(CardPredicate filter) {
        this(new Fixed(1), filter, LibrarySearchDestination.HAND, null, 1);
    }

    /** Single card matching {@code filter} to the given destination. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(1), filter, destination, null, 1);
    }

    /** Up to {@code count} cards matching {@code filter} to the given destination. */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination) {
        this(count, filter, destination, null, 1);
    }

    /**
     * Tutor for {@code count} cards to hand normally, {@code castFromGraveyardCount} when cast from a
     * graveyard (flashback). A {@code null} filter is an unrestricted tutor (e.g. Increasing Ambition).
     */
    public SearchLibraryEffect(CardPredicate filter, int count, int castFromGraveyardCount) {
        this(new Fixed(count), filter, LibrarySearchDestination.HAND, null, castFromGraveyardCount);
    }

    /** Single card matching {@code filter} to the given destination with an X-relative mana-value bound. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination, XManaValueBound manaValueBound) {
        this(new Fixed(1), filter, destination, manaValueBound, 1);
    }
}
