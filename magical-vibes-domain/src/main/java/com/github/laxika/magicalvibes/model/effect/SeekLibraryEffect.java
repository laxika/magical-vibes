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
 */
public record SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                                LibrarySearchDestination destination, ManaValueBound manaValueBound,
                                boolean faceDown, boolean grantPlayUntilNextTurn, CardSubtype grantSubtype,
                                boolean battlefieldCloaked)
        implements TriggeringSpellManaValueEffect {

    public SeekLibraryEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(nonNegative(count)), filter, destination, null, false, false, null, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination) {
        this(count, filter, destination, null, false, false, null, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound) {
        this(count, filter, destination, manaValueBound, false, false, null, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, false, false, grantSubtype, false);
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination,
                             ManaValueBound manaValueBound, CardSubtype grantSubtype) {
        this(new Fixed(nonNegative(count)), filter, destination, manaValueBound, false, false, grantSubtype, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown) {
        this(count, filter, destination, manaValueBound, faceDown, false, null, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, boolean grantPlayUntilNextTurn) {
        this(count, filter, destination, manaValueBound, faceDown, grantPlayUntilNextTurn, null, false);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination, ManaValueBound manaValueBound,
                             boolean faceDown, CardSubtype grantSubtype) {
        this(count, filter, destination, manaValueBound, faceDown, false, grantSubtype, false);
    }

    public SeekLibraryEffect(int count, CardPredicate filter) {
        this(new Fixed(nonNegative(count)), filter, LibrarySearchDestination.HAND, null, false, false, null, false);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue, boolean entersTapped) {
        this(new Fixed(1), filter,
                entersTapped ? LibrarySearchDestination.BATTLEFIELD_TAPPED
                        : LibrarySearchDestination.BATTLEFIELD,
                new ManaValueBound(new Fixed(maxManaValue), false, 0), false, false, null, false);
    }

    public SeekLibraryEffect(CardPredicate filter, int maxManaValue) {
        this(filter, maxManaValue, false);
    }

    public static SeekLibraryEffect cloakedToBattlefield(DynamicAmount count, CardPredicate filter,
                                                          ManaValueBound manaValueBound) {
        return new SeekLibraryEffect(count, filter, LibrarySearchDestination.BATTLEFIELD,
                manaValueBound, false, false, null, true);
    }

    private static int nonNegative(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        return count;
    }
}
