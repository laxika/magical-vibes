package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Set;

/**
 * Search target player's library for up to {@code count} cards matching {@code filter} and move them
 * to {@code destination}, then that player shuffles. Targets a player — the target-library mirror of
 * {@link SearchLibraryEffect}, which searches the controller's own library.
 *
 * <p>A {@code null} filter is an unrestricted search (any card): every card in the library is a
 * candidate. Ordinary exile is face up; destinations that explicitly grant hidden play permission
 * handle their own face-down exile. A non-null filter narrows candidates and drives the prompt wording.
 *
 * <p>{@code canFailToFind} separates a search for a bare quantity ("three cards"), where the searcher
 * must find that many if they are there (CR 701.23d), from a search for cards with a stated quality or
 * an "up to" wording, neither of which obliges the searcher to find anything (CR 701.23b).
 *
 * <p>{@code searchPlayer} is who searches: {@code you()}, or the targeted player searching their own library
 * ({@code targetPlayerDeclaredElsewhere()}: this effect's own target spec declares the player).
 *
 * <p>Replaced the {@code SearchTargetLibraryFor*} family (cards-to-exile, cards-to-graveyard,
 * card-to-battlefield-under-your-control, and card-to-exile-with-play-permission). {@code CAST_WITHOUT_PAYING} is
 * "you may cast that card without paying its mana cost" (Knowledge Exploitation): a card that isn't cast stays in that
 * library, which is shuffled.
 */
public record SearchTargetLibraryEffect(DynamicAmount count,
                                        CardPredicate filter,
                                        LibrarySearchDestination destination,
                                        boolean canFailToFind,
                                        PlayerSubject searchPlayer) implements CardEffect {

    /**
     * The destinations a target-library search knows how to reach. Anything else would fall through
     * the handler's prompt and payload switches, so it is rejected at card-construction time.
     */
    private static final Set<LibrarySearchDestination> SUPPORTED_DESTINATIONS = Set.of(
            LibrarySearchDestination.EXILE,
            LibrarySearchDestination.EXILE_PLAYABLE,
            LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP,
            LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_END_OF_TURN,
            LibrarySearchDestination.GRAVEYARD,
            LibrarySearchDestination.BATTLEFIELD_UNDER_SEARCHER,
            LibrarySearchDestination.HAND,
            LibrarySearchDestination.CAST_WITHOUT_PAYING);

    public SearchTargetLibraryEffect {
        if (!SUPPORTED_DESTINATIONS.contains(destination)) {
            throw new IllegalArgumentException(
                    "SearchTargetLibraryEffect does not support destination " + destination);
        }
        if (!(searchPlayer instanceof PlayerSubject.You
                || searchPlayer instanceof PlayerSubject.TargetPlayer target && !target.declaresTarget())) {
            throw new IllegalArgumentException("SearchTargetLibraryEffect is searched by you() or by the targeted player"
                    + " (targetPlayerDeclaredElsewhere(), this effect declares the target)");
        }
    }

    /** Dynamic-count search with the controller as the searcher. */
    public SearchTargetLibraryEffect(DynamicAmount count, CardPredicate filter,
                                     LibrarySearchDestination destination, boolean canFailToFind) {
        this(count, filter, destination, canFailToFind, Subjects.you());
    }

    /** Fixed-count search (Jester's Cap, Life's Finale, Bribery, Praetor's Grasp). */
    public SearchTargetLibraryEffect(int count, CardPredicate filter,
                                     LibrarySearchDestination destination, boolean canFailToFind) {
        this(new Fixed(count), filter, destination, canFailToFind, Subjects.you());
    }

    /** Fixed-count search with an explicitly chosen searcher (e.g. a target player searches their own library). */
    public SearchTargetLibraryEffect(int count, CardPredicate filter,
                                     LibrarySearchDestination destination, boolean canFailToFind,
                                     PlayerSubject searchPlayer) {
        this(new Fixed(count), filter, destination, canFailToFind, searchPlayer);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
