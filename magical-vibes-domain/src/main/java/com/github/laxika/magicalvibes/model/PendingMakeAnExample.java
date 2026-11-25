package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Progress state for Make an Example. Its three sentences are three actions (CR 608.2e): every opponent separates
 * their creatures, in APNAP order ({@code currentOpponentIndex}, the {@link Separation}s made so far); then the
 * controller chooses a pile for each opponent ({@code currentChoiceIndex}, the creatures of the piles chosen so far in
 * {@code chosenToSacrifice}); then every chosen creature is sacrificed at once.
 */
public record PendingMakeAnExample(UUID controllerId, List<UUID> opponentIds,
                                   int currentOpponentIndex, String sourceName,
                                   List<Separation> separations, int currentChoiceIndex,
                                   List<UUID> chosenToSacrifice)
        implements PendingInteraction {

    /** One opponent's two piles. */
    public record Separation(UUID opponentId, List<UUID> pile1Ids, List<UUID> pile2Ids) {
        public Separation {
            pile1Ids = List.copyOf(pile1Ids);
            pile2Ids = List.copyOf(pile2Ids);
        }
    }

    public PendingMakeAnExample {
        opponentIds = List.copyOf(opponentIds);
        separations = List.copyOf(separations);
        chosenToSacrifice = List.copyOf(chosenToSacrifice);
    }

    /** The state before any opponent has separated. */
    public PendingMakeAnExample(UUID controllerId, List<UUID> opponentIds, String sourceName) {
        this(controllerId, opponentIds, 0, sourceName, List.of(), 0, List.of());
    }

    /** The next opponent is to separate. */
    public PendingMakeAnExample nextOpponent() {
        return new PendingMakeAnExample(controllerId, opponentIds, currentOpponentIndex + 1, sourceName,
                separations, currentChoiceIndex, chosenToSacrifice);
    }

    /** The current opponent separated their creatures into {@code separation}; the next one is to separate. */
    public PendingMakeAnExample withSeparation(Separation separation) {
        List<Separation> all = new ArrayList<>(separations);
        all.add(separation);
        return new PendingMakeAnExample(controllerId, opponentIds, currentOpponentIndex + 1, sourceName,
                all, currentChoiceIndex, chosenToSacrifice);
    }

    /** The controller chose {@code chosenPile} for the current separation; the next separation is to be chosen. */
    public PendingMakeAnExample withChosenPile(List<UUID> chosenPile) {
        List<UUID> all = new ArrayList<>(chosenToSacrifice);
        all.addAll(chosenPile);
        return new PendingMakeAnExample(controllerId, opponentIds, currentOpponentIndex, sourceName,
                separations, currentChoiceIndex + 1, all);
    }

    @Override
    public UUID decidingPlayerId() {
        return controllerId;
    }

    @Override
    public InteractionOptions legalOptions() {
        return InteractionOptions.UNENUMERATED;
    }
}
