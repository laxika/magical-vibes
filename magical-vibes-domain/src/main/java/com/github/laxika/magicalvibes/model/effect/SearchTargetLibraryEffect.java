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
 * candidate, and because such a search reveals nothing an exiled card is exiled face down. A non-null
 * filter narrows the candidates, drives the prompt wording, and exiles the found card face up.
 *
 * <p>{@code canFailToFind} separates a search for a bare quantity ("three cards"), where the searcher
 * must find that many if they are there (CR 701.23d), from a search for cards with a stated quality or
 * an "up to" wording, neither of which obliges the searcher to find anything (CR 701.23b).
 *
 * <p>Replaced the {@code SearchTargetLibraryFor*} family (cards-to-exile, cards-to-graveyard,
 * card-to-battlefield-under-your-control, and card-to-exile-with-play-permission).
 */
public record SearchTargetLibraryEffect(DynamicAmount count,
                                        CardPredicate filter,
                                        LibrarySearchDestination destination,
                                        boolean canFailToFind) implements CombatDamageTriggerContextEffect {

    /**
     * The destinations a target-library search knows how to reach. Anything else would fall through
     * the handler's prompt and payload switches, so it is rejected at card-construction time.
     */
    private static final Set<LibrarySearchDestination> SUPPORTED_DESTINATIONS = Set.of(
            LibrarySearchDestination.EXILE,
            LibrarySearchDestination.EXILE_PLAYABLE,
            LibrarySearchDestination.EXILE_PLAYABLE_UNTIL_NEXT_UPKEEP,
            LibrarySearchDestination.GRAVEYARD,
            LibrarySearchDestination.BATTLEFIELD_UNDER_SEARCHER);

    public SearchTargetLibraryEffect {
        if (!SUPPORTED_DESTINATIONS.contains(destination)) {
            throw new IllegalArgumentException(
                    "SearchTargetLibraryEffect does not support destination " + destination);
        }
    }

    /** Fixed-count search (Jester's Cap, Life's Finale, Bribery, Praetor's Grasp). */
    public SearchTargetLibraryEffect(int count, CardPredicate filter,
                                     LibrarySearchDestination destination, boolean canFailToFind) {
        this(new Fixed(count), filter, destination, canFailToFind);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
