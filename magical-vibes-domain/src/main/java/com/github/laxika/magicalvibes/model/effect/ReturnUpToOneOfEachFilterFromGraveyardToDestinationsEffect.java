package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Returns up to one target card matching each filter from the controller's graveyard, with one
 * destination per target group.
 */
public record ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
        List<CardPredicate> targetFilters,
        List<GraveyardChoiceDestination> destinations,
        List<String> targetDescriptions
) implements IndependentlyTargetedGraveyardCardsEffect {

    public ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect {
        if (targetFilters == null || targetFilters.isEmpty()
                || destinations == null || targetDescriptions == null
                || targetFilters.size() != destinations.size()
                || targetFilters.size() != targetDescriptions.size()) {
            throw new IllegalArgumentException("Target filters, destinations, and descriptions must have equal non-zero sizes");
        }
        targetFilters = List.copyOf(targetFilters);
        destinations = List.copyOf(destinations);
        targetDescriptions = List.copyOf(targetDescriptions);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
