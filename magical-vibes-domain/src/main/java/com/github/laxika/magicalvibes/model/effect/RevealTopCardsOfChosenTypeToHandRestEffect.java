package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Reveals the top cards of the controller's library, putting cards of a chosen type into their
 * hand and the rest in the configured destination.
 */
public record RevealTopCardsOfChosenTypeToHandRestEffect(int count, LookDestination restDestination)
        implements CardEffect {

    public RevealTopCardsOfChosenTypeToHandRestEffect(int count) {
        this(count, LookDestination.BOTTOM_OF_LIBRARY);
    }

    public RevealTopCardsOfChosenTypeToHandRestEffect {
        Objects.requireNonNull(restDestination, "restDestination");
        if (restDestination != LookDestination.BOTTOM_OF_LIBRARY
                && restDestination != LookDestination.GRAVEYARD) {
            throw new IllegalArgumentException("Unsupported rest destination: " + restDestination);
        }
    }
}
