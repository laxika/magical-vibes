package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.LibrarySearchPlayer;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.FixedIfTargetPlayerControlsMoreLands;
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
 * <p>{@code manaValueBound} adds a dynamic mana-value constraint (see {@link ManaValueBound});
 * {@code null} means none. {@code castFromGraveyardCount} overrides {@code count} when the producing
 * spell was cast from a graveyard (flashback) — see Increasing Ambition (1 normally, 2 from a
 * graveyard). {@code requireDifferentNames} (Uncage the Menagerie) excludes cards whose names match
 * an already-chosen pick for the same search.
 *
 * <p>{@code grantHaste}, {@code exileAtEndStep}, and {@code returnToHandAtEndStep} apply to battlefield
 * destinations only: the found permanent gains haste, and/or is exiled or returned to its owner's
 * hand at the beginning of the next end step (Zirilan of the Claw, Nahiri, the Harbinger).
 * {@code animateFound} likewise applies to battlefield destinations only: every permanent the search
 * put onto the battlefield is animated by that {@link AnimatePermanentsEffect} as it enters (Nissa,
 * Worldwaker's "those lands become 4/4 Elemental creatures with trample").
 * {@code searchPlayer} selects whose library is searched; it defaults to the stack entry's
 * controller and can use the active player for effects such as Oath of Lieges.
 * {@code shuffleAfterSelection} controls whether the search interaction shuffles immediately after
 * the selected card is moved. Set it to false when a later effect must resolve before the shuffle.
 *
 * @param onlyIfSacrificed when true, an {@code ON_DEATH} trigger only fires when its source was
 *                         sacrificed
 *
 * <p>Replaced the {@code SearchLibraryFor*} family (to-hand tutors, by-name searches, to-top,
 * creature-to-battlefield with MV/colour/subtype constraints, and card-types-to-battlefield).
 */
public record SearchLibraryEffect(
        DynamicAmount count,
        CardPredicate filter,
        LibrarySearchDestination destination,
        ManaValueBound manaValueBound,
        int castFromGraveyardCount,
        boolean requireDifferentNames,
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean returnToHandAtEndStep,
        AnimatePermanentsEffect animateFound,
        LibrarySearchPlayer searchPlayer,
        boolean onlyIfSacrificed,
        boolean battlefieldIfChosenBeholdType,
        boolean shuffleAfterSelection
) implements CardEffect {

    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               ManaValueBound manaValueBound, int castFromGraveyardCount,
                               boolean requireDifferentNames, boolean grantHaste, boolean exileAtEndStep,
                               AnimatePermanentsEffect animateFound) {
        this(count, filter, destination, manaValueBound, castFromGraveyardCount, requireDifferentNames,
                grantHaste, exileAtEndStep, false, animateFound, LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               ManaValueBound manaValueBound, int castFromGraveyardCount,
                               boolean requireDifferentNames, boolean grantHaste, boolean exileAtEndStep,
                               AnimatePermanentsEffect animateFound, LibrarySearchPlayer searchPlayer,
                               boolean onlyIfSacrificed) {
        this(count, filter, destination, manaValueBound, castFromGraveyardCount, requireDifferentNames,
                grantHaste, exileAtEndStep, false, animateFound, searchPlayer, onlyIfSacrificed, false, true);
    }

    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               ManaValueBound manaValueBound, int castFromGraveyardCount,
                               boolean requireDifferentNames, boolean grantHaste, boolean exileAtEndStep,
                               AnimatePermanentsEffect animateFound, boolean battlefieldIfChosenBeholdType) {
        this(count, filter, destination, manaValueBound, castFromGraveyardCount, requireDifferentNames,
                grantHaste, exileAtEndStep, false, animateFound, LibrarySearchPlayer.CONTROLLER, false,
                battlefieldIfChosenBeholdType, true);
    }

    /** Unrestricted single-card tutor to hand (e.g. Diabolic Tutor). */
    public SearchLibraryEffect() {
        this(new Fixed(1), null, LibrarySearchDestination.HAND, null, 1, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Single card matching {@code filter} to hand (basic land, artifact, creature, …). */
    public SearchLibraryEffect(CardPredicate filter) {
        this(new Fixed(1), filter, LibrarySearchDestination.HAND, null, 1, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Single card matching {@code filter} to the given destination. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(1), filter, destination, null, 1, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Up to {@code count} cards matching {@code filter} to the given destination. */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination) {
        this(count, filter, destination, null, 1, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /**
     * Tutor for {@code count} cards to hand normally, {@code castFromGraveyardCount} when cast from a
     * graveyard (flashback). A {@code null} filter is an unrestricted tutor (e.g. Increasing Ambition).
     */
    public SearchLibraryEffect(CardPredicate filter, int count, int castFromGraveyardCount) {
        this(new Fixed(count), filter, LibrarySearchDestination.HAND, null, castFromGraveyardCount, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Single card matching {@code filter} to the given destination with a dynamic mana-value bound. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination, ManaValueBound manaValueBound) {
        this(new Fixed(1), filter, destination, manaValueBound, 1, false, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /**
     * Up to {@code count} cards matching {@code filter} to {@code destination} with a mana-value bound
     * and optional distinct-names constraint (Uncage the Menagerie).
     */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               ManaValueBound manaValueBound, boolean requireDifferentNames) {
        this(count, filter, destination, manaValueBound, 1, requireDifferentNames, false, false, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /**
     * Single card matching {@code filter} onto the battlefield, optionally with haste and/or exiled
     * at the beginning of the next end step (Zirilan of the Claw).
     */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination,
                               boolean grantHaste, boolean exileAtEndStep) {
        this(new Fixed(1), filter, destination, null, 1, false, grantHaste, exileAtEndStep, false, null,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Single card matching {@code filter} onto the battlefield with haste and a delayed return to hand. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination,
                               boolean grantHaste, boolean exileAtEndStep, boolean returnToHandAtEndStep) {
        this(new Fixed(1), filter, destination, null, 1, false, grantHaste, exileAtEndStep,
                returnToHandAtEndStep, null, LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /**
     * Up to {@code count} cards matching {@code filter} onto the battlefield, each animated by
     * {@code animateFound} as it enters (Nissa, Worldwaker).
     */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, AnimatePermanentsEffect animateFound) {
        this(count, filter, LibrarySearchDestination.BATTLEFIELD, null, 1, false, false, false, false, animateFound,
                LibrarySearchPlayer.CONTROLLER, false, false, true);
    }

    /** Single-card search using the specified player as the library owner. */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               LibrarySearchPlayer searchPlayer) {
        this(count, filter, destination, null, 1, false, false, false, false, null, searchPlayer, false, false, true);
    }

    /** Up to {@code count} matching cards to the battlefield tapped, only after this source was sacrificed. */
    public static SearchLibraryEffect sacrificeOnly(DynamicAmount count, CardPredicate filter,
                                                    LibrarySearchDestination destination) {
        return new SearchLibraryEffect(count, filter, destination, null, 1, false,
                false, false, false, null, LibrarySearchPlayer.CONTROLLER, true, false, true);
    }

    /** Unrestricted single-card tutor that leaves the library shuffle to a later effect. */
    public static SearchLibraryEffect withDeferredShuffle() {
        return new SearchLibraryEffect(new Fixed(1), null, LibrarySearchDestination.HAND, null, 1,
                false, false, false, false, null, LibrarySearchPlayer.CONTROLLER, false, false, false);
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }

    @Override
    public TargetSpec targetSpec() {
        // Tithe: count scales off whether the targeted opponent controls more lands.
        return count instanceof FixedIfTargetPlayerControlsMoreLands
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
