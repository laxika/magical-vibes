package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;
import java.util.Objects;

/**
 * Reveals the top cards of the controller's library, putting cards of a chosen type into their
 * hand and the rest in the configured destination.
 */
public record RevealTopCardsOfChosenTypeToHandRestEffect(int count, LookDestination restDestination,
                                                        List<CardType> allowedTypes)
        implements CardEffect {

    public RevealTopCardsOfChosenTypeToHandRestEffect(int count) {
        this(count, LookDestination.BOTTOM_OF_LIBRARY, List.of(CardType.values()));
    }

    public RevealTopCardsOfChosenTypeToHandRestEffect(int count, LookDestination restDestination) {
        this(count, restDestination, List.of(CardType.values()));
    }

    public RevealTopCardsOfChosenTypeToHandRestEffect {
        Objects.requireNonNull(restDestination, "restDestination");
        Objects.requireNonNull(allowedTypes, "allowedTypes");
        allowedTypes = List.copyOf(allowedTypes);
        if (allowedTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one card type must be allowed");
        }
        if (restDestination != LookDestination.BOTTOM_OF_LIBRARY
                && restDestination != LookDestination.GRAVEYARD) {
            throw new IllegalArgumentException("Unsupported rest destination: " + restDestination);
        }
    }
}
