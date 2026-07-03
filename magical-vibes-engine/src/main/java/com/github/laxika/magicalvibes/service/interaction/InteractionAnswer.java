package com.github.laxika.magicalvibes.service.interaction;

/**
 * A player's answer to a {@link com.github.laxika.magicalvibes.model.PendingInteraction},
 * as it arrives from the wire (via {@code GameService}'s answer entry points). Each record
 * mirrors one wire-message payload shape; new shapes are added as interaction kinds migrate
 * to the {@link InteractionHandlerRegistry}.
 */
public sealed interface InteractionAnswer {

    /** A single non-negative number (e.g. an X value). */
    record NumberChosen(int value) implements InteractionAnswer {
    }

    /** A split of viewed cards into a top-of-library ordering and a bottom-of-library ordering. */
    record ScryOrder(java.util.List<Integer> topCardOrder, java.util.List<Integer> bottomCardOrder)
            implements InteractionAnswer {
    }

    /** One viewed card to hand and one on top of the library (the rest go to the bottom). */
    record HandTopBottom(int handCardIndex, int topCardIndex) implements InteractionAnswer {
    }

    /** A full ordering of the viewed cards (indices into the viewed list). */
    record CardOrder(java.util.List<Integer> cardOrder) implements InteractionAnswer {
    }

    /** Accept or decline a may ability. */
    record MayAbilityChosen(boolean accepted) implements InteractionAnswer {
    }

    /** A selection of zero or more cards by ID (the shared multiple-cards wire payload). */
    record CardsChosen(java.util.List<java.util.UUID> cardIds) implements InteractionAnswer {
    }
}
