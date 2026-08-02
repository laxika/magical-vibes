package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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
 * <p>{@code grantHaste} and {@code exileAtEndStep} apply to battlefield destinations only: the found
 * permanent gains haste, and/or is exiled at the beginning of the next end step (Zirilan of the Claw).
 * {@code animateFound} likewise applies to battlefield destinations only: every permanent the search
 * put onto the battlefield is animated by that {@link AnimatePermanentsEffect} as it enters (Nissa,
 * Worldwaker's "those lands become 4/4 Elemental creatures with trample").
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
        AnimatePermanentsEffect animateFound
) implements CardEffect {

    /** Unrestricted single-card tutor to hand (e.g. Diabolic Tutor). */
    public SearchLibraryEffect() {
        this(new Fixed(1), null, LibrarySearchDestination.HAND, null, 1, false, false, false, null);
    }

    /** Single card matching {@code filter} to hand (basic land, artifact, creature, …). */
    public SearchLibraryEffect(CardPredicate filter) {
        this(new Fixed(1), filter, LibrarySearchDestination.HAND, null, 1, false, false, false, null);
    }

    /** Single card matching {@code filter} to the given destination. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(1), filter, destination, null, 1, false, false, false, null);
    }

    /** Up to {@code count} cards matching {@code filter} to the given destination. */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination) {
        this(count, filter, destination, null, 1, false, false, false, null);
    }

    /**
     * Tutor for {@code count} cards to hand normally, {@code castFromGraveyardCount} when cast from a
     * graveyard (flashback). A {@code null} filter is an unrestricted tutor (e.g. Increasing Ambition).
     */
    public SearchLibraryEffect(CardPredicate filter, int count, int castFromGraveyardCount) {
        this(new Fixed(count), filter, LibrarySearchDestination.HAND, null, castFromGraveyardCount, false, false, false, null);
    }

    /** Single card matching {@code filter} to the given destination with a dynamic mana-value bound. */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination, ManaValueBound manaValueBound) {
        this(new Fixed(1), filter, destination, manaValueBound, 1, false, false, false, null);
    }

    /**
     * Up to {@code count} cards matching {@code filter} to {@code destination} with a mana-value bound
     * and optional distinct-names constraint (Uncage the Menagerie).
     */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, LibrarySearchDestination destination,
                               ManaValueBound manaValueBound, boolean requireDifferentNames) {
        this(count, filter, destination, manaValueBound, 1, requireDifferentNames, false, false, null);
    }

    /**
     * Single card matching {@code filter} onto the battlefield, optionally with haste and/or exiled
     * at the beginning of the next end step (Zirilan of the Claw).
     */
    public SearchLibraryEffect(CardPredicate filter, LibrarySearchDestination destination,
                               boolean grantHaste, boolean exileAtEndStep) {
        this(new Fixed(1), filter, destination, null, 1, false, grantHaste, exileAtEndStep, null);
    }

    /**
     * Up to {@code count} cards matching {@code filter} onto the battlefield, each animated by
     * {@code animateFound} as it enters (Nissa, Worldwaker).
     */
    public SearchLibraryEffect(DynamicAmount count, CardPredicate filter, AnimatePermanentsEffect animateFound) {
        this(count, filter, LibrarySearchDestination.BATTLEFIELD, null, 1, false, false, false, animateFound);
    }

    @Override
    public TargetSpec targetSpec() {
        // Tithe: count scales off whether the targeted opponent controls more lands.
        return count instanceof FixedIfTargetPlayerControlsMoreLands
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.NONE;
    }
}
