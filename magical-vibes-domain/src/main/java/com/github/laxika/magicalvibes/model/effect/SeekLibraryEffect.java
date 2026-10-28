package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Seeks up to {@code count} matching cards from the controller's library at random and puts them
 * into the configured destination. Seek does not give the player a choice and does not shuffle the
 * library.
 *
 * <p>When {@code triggersSeekAbilities} is false the cards are taken at random without being sought,
 * so "whenever you seek" abilities do not trigger (Tezzeret's Reckoning exiles random cards, and
 * {@link #randomExileWithSource(DynamicAmount, CardPredicate, boolean)} builds that form).
 */
public record SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                                LibrarySearchDestination destination, ManaValueBound manaValueBound,
                                boolean faceDown, boolean grantPlayUntilNextTurn, CardSubtype grantSubtype,
                                boolean battlefieldCloaked, boolean triggersSeekAbilities)
        implements TriggeringSpellManaValueEffect, TriggeringPermanentManaValueEffect {

    public SeekLibraryEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(nonNegative(count)), filter, destination, null, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination) {
        this(count, filter, destination, null, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound) {
        this(count, filter, destination, manaValueBound, false, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, false, false, grantSubtype, false, true);
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination,
                             ManaValueBound manaValueBound, CardSubtype grantSubtype) {
        this(new Fixed(nonNegative(count)), filter, destination, manaValueBound, false, false, grantSubtype,
                false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown) {
        this(count, filter, destination, manaValueBound, faceDown, false, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, boolean grantPlayUntilNextTurn) {
        this(count, filter, destination, manaValueBound, faceDown, grantPlayUntilNextTurn, null, false, true);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, faceDown, false, grantSubtype, false, true);
    }

    public SeekLibraryEffect(int count, CardPredicate filter) {
        this(new Fixed(nonNegative(count)), filter, LibrarySearchDestination.HAND, null, false, false, null,
                false, true);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue, boolean entersTapped) {
        this(new Fixed(1), filter,
                entersTapped ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                        : LibrarySearchDestination.BATTLEFIELD,
                new ManaValueBound(new Fixed(maxManaValue), false, 0), false, false, null, false, true);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue) {
        this(filter, maxManaValue, false);
    }

    public static SeekLibraryEffect cloakedToBattlefield(DynamicAmount count, CardPredicate filter,
                                                          ManaValueBound manaValueBound) {
        return new SeekLibraryEffect(count, filter, LibrarySearchDestination.BATTLEFIELD,
                manaValueBound, false, false, null, true, true);
    }

    /**
     * Randomly takes {@code count} matching cards from the library and exiles them tracked with the
     * source, without seeking them: "whenever you seek" abilities do not trigger.
     */
    public static SeekLibraryEffect randomExileWithSource(DynamicAmount count, CardPredicate filter,
                                                          boolean faceDown) {
        return new SeekLibraryEffect(count, filter, LibrarySearchDestination.EXILE_WITH_SOURCE, null,
                faceDown, false, null, false, false);
    }

    /**
     * Randomly takes {@code count} matching cards from the library and puts them into the destination
     * without seeking them: "whenever you seek" abilities do not trigger.
     */
    public static SeekLibraryEffect randomWithoutSeeking(DynamicAmount count, CardPredicate filter,
                                                         LibrarySearchDestination destination,
                                                         ManaValueBound manaValueBound) {
        return new SeekLibraryEffect(count, filter, destination, manaValueBound, false, false, null, false,
                false);
    }

    private static int nonNegative(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        return count;
    }
}
