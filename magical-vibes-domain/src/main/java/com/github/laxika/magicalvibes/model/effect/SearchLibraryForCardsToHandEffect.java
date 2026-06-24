package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Search your library for {@code count} cards matching {@code filter} and put them into your hand,
 * then shuffle. If the spell that produced this effect was cast from a graveyard (e.g. via
 * flashback), {@code castFromGraveyardCount} cards are searched for instead.
 *
 * <p>A {@code null} filter means an unrestricted search (any card): the cards are not revealed and
 * the search cannot fail to find. A non-null filter restricts the search: the chosen cards are
 * revealed and the search may fail to find. The prompt text is derived from the filter.
 *
 * <p>For a plain single-card tutor pass {@code count == castFromGraveyardCount == 1} (the
 * convenience constructors do this). Used by Diabolic Tutor (any), Increasing Ambition
 * (any, 1 / 2 from a graveyard), basic-land fetchers, Fabricate, Trinket Mage, and others.
 */
public record SearchLibraryForCardsToHandEffect(
        CardPredicate filter,
        int count,
        int castFromGraveyardCount
) implements CardEffect {

    /** Unrestricted search for a single card to hand (e.g. Diabolic Tutor). */
    public SearchLibraryForCardsToHandEffect() {
        this(null, 1, 1);
    }

    /** Search for a single card matching {@code filter} to hand (e.g. basic land, artifact). */
    public SearchLibraryForCardsToHandEffect(CardPredicate filter) {
        this(filter, 1, 1);
    }
}
