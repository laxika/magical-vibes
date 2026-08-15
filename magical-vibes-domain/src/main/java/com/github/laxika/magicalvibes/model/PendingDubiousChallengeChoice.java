package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Carries Dubious Challenge's controller and opponent choice state across the shared
 * {@link PendingInteraction.LibraryRevealChoice} interaction.
 */
public record PendingDubiousChallengeChoice(UUID controllerId, UUID opponentId, List<Card> exiledCards)
        implements PendingInteraction {

    public PendingDubiousChallengeChoice {
        exiledCards = List.copyOf(exiledCards);
    }
}
