package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Holds Truth or Tale's second resolution-time choice after the opponent has selected a pile.
 * The chosen pile is offered for one card to hand; the remaining cards from both piles go to the
 * bottom of the controller's library in an order they choose.
 */
public record PendingTruthOrTaleCardChoice(UUID controllerId, List<Card> chosenPileCards,
                                           List<Card> otherCards) implements PendingInteraction {

    public PendingTruthOrTaleCardChoice {
        chosenPileCards = List.copyOf(chosenPileCards);
        otherCards = List.copyOf(otherCards);
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
