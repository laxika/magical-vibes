package com.github.laxika.magicalvibes.model.effect;

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
                                LibrarySearchDestination destination, ManaValueBound manaValueBound)
        implements CardEffect {

    public SeekLibraryEffect {
        Objects.requireNonNull(count, "count");
        Objects.requireNonNull(destination, "destination");
    }

    public SeekLibraryEffect(int count, CardPredicate filter, LibrarySearchDestination destination) {
        this(new Fixed(nonNegative(count)), filter, destination, null);
    }

    public SeekLibraryEffect(DynamicAmount count, CardPredicate filter,
                             LibrarySearchDestination destination) {
        this(count, filter, destination, null);
    }

    public SeekLibraryEffect(int count, CardPredicate filter) {
        this(new Fixed(nonNegative(count)), filter, LibrarySearchDestination.HAND, null);
    }

    private static int nonNegative(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
        return count;
    }
}
