package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/** Holds Manifold Insights' remaining opponent choices while its revealed cards are out of the library. */
public record PendingManifoldInsightsChoice(UUID controllerId, List<UUID> remainingOpponentIds,
                                            List<Card> remainingCards) implements PendingInteraction {
}
