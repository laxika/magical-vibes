package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Collections;
import java.util.List;

/**
 * Returns up to one target card matching each filter from the controller's graveyard, with one
 * destination per target group.
 */
public record ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
        List<CardPredicate> targetFilters,
        List<GraveyardChoiceDestination> destinations,
        List<String> targetDescriptions,
        List<Integer> minimumTargetCounts,
        boolean requiresDistinctTargets,
        boolean enterTapped
) implements IndependentlyTargetedGraveyardCardsEffect {

    public ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
            List<CardPredicate> targetFilters,
            List<GraveyardChoiceDestination> destinations,
            List<String> targetDescriptions) {
        this(targetFilters, destinations, targetDescriptions,
                targetFilters == null ? List.of() : Collections.nCopies(targetFilters.size(), 0), false, false);
    }

    public ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect(
            List<CardPredicate> targetFilters,
            List<GraveyardChoiceDestination> destinations,
            List<String> targetDescriptions,
            List<Integer> minimumTargetCounts,
            boolean requiresDistinctTargets) {
        this(targetFilters, destinations, targetDescriptions, minimumTargetCounts, requiresDistinctTargets, false);
    }

    public ReturnUpToOneOfEachFilterFromGraveyardToDestinationsEffect {
        if (targetFilters == null || targetFilters.isEmpty()
                || destinations == null || targetDescriptions == null
                || minimumTargetCounts == null
                || targetFilters.size() != destinations.size()
                || targetFilters.size() != targetDescriptions.size()
                || targetFilters.size() != minimumTargetCounts.size()) {
            throw new IllegalArgumentException("Target filters, destinations, descriptions, and minimum counts must have equal non-zero sizes");
        }
        if (minimumTargetCounts.stream().anyMatch(count -> count < 0 || count > 1)) {
            throw new IllegalArgumentException("Minimum target counts must be zero or one");
        }
        targetFilters = List.copyOf(targetFilters);
        destinations = List.copyOf(destinations);
        targetDescriptions = List.copyOf(targetDescriptions);
        minimumTargetCounts = List.copyOf(minimumTargetCounts);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }
}
