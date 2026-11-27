package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * What a resolution goes on with once its controller has chosen the opponent an "an opponent" instruction names, in
 * the middle of that resolution: after an exile, a search, a reveal or a pile choice, when the instruction is applied
 * (CR 608.2d). Each record holds what its flow has done so far; the engine's {@code ChosenOpponentSupport} asks the
 * question and hands the answer to the flow's continuation.
 */
public sealed interface ChosenOpponentFollowUp {

    /** Abstract Performance: the two exiled piles, before an opponent chooses the one put into the graveyard. */
    record AbstractPerformancePiles(Card sourceCard, UUID controllerId, List<Card> faceDownPile,
                                    List<Card> faceUpPile) implements ChosenOpponentFollowUp {
        public AbstractPerformancePiles {
            faceDownPile = List.copyOf(faceDownPile);
            faceUpPile = List.copyOf(faceUpPile);
        }
    }

    /** Brilliant Ultimatum: the exiled cards, before an opponent separates them into two piles. */
    record BrilliantUltimatumPiles(UUID controllerId, List<Card> exiledCards) implements ChosenOpponentFollowUp {
        public BrilliantUltimatumPiles {
            exiledCards = List.copyOf(exiledCards);
        }
    }

    /** Hostile Negotiations: the pile turned face up, before an opponent chooses a pile (the piles stay queued). */
    record HostileNegotiationsPiles(boolean pile1FaceUp) implements ChosenOpponentFollowUp {}

    /** Ecological Appreciation: the revealed cards, before an opponent chooses two of them. */
    record EcologicalAppreciationReveal(UUID controllerId, List<Card> revealedCards, Set<UUID> graveyardCardIds)
            implements ChosenOpponentFollowUp {
        public EcologicalAppreciationReveal {
            revealedCards = List.copyOf(revealedCards);
            graveyardCardIds = Set.copyOf(graveyardCardIds);
        }
    }

    /** Threats Undetected: the revealed cards, before an opponent chooses two of them. */
    record ThreatsUndetectedReveal(UUID controllerId, List<Card> revealedCards) implements ChosenOpponentFollowUp {
        public ThreatsUndetectedReveal {
            revealedCards = List.copyOf(revealedCards);
        }
    }

    /** Turtles Forever: the revealed cards, before an opponent chooses two of them. */
    record TurtlesForeverReveal(UUID controllerId, List<Card> revealedCards) implements ChosenOpponentFollowUp {
        public TurtlesForeverReveal {
            revealedCards = List.copyOf(revealedCards);
        }
    }

    /** Verdant Mastery cast for {3}{G}: the revealed basic lands, before one is put under an opponent's control. */
    record VerdantMasteryReveal(UUID controllerId, List<Card> revealedLands) implements ChosenOpponentFollowUp {
        public VerdantMasteryReveal {
            revealedLands = List.copyOf(revealedLands);
        }
    }
}
