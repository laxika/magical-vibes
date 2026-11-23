package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;
import java.util.Set;

/**
 * "Its controller conjures a card named … onto the battlefield": the card of the printing named by {@code setCode} and
 * {@code collectorNumber} enters under the control of the triggering permanent's controller, as it is or last was
 * ({@code controllerOf(triggering())}); a permanent of one of {@code enterTappedTypes} enters tapped.
 */
public record ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect(
        String setCode,
        String collectorNumber,
        Set<CardType> enterTappedTypes
) implements CardEffect {

    public ConjureCardOntoBattlefieldForTriggeringPermanentControllerEffect {
        Objects.requireNonNull(setCode, "Set code cannot be null");
        Objects.requireNonNull(collectorNumber, "Collector number cannot be null");
        enterTappedTypes = Set.copyOf(Objects.requireNonNull(enterTappedTypes,
                "Enter-tapped types cannot be null"));
    }
}
