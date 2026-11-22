package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Put them into their owners' graveyards": each card {@code cards} names that is in exile, or revealed in a library
 * ({@code Subjects.cardsThisWay(StepKind.REVEAL)}: Dakra Mystic, "puts the rest into their graveyard"), goes to its
 * owner's graveyard, face-down ones included. The step records how many were put into a graveyard this way
 * ({@code StepKind.PUT_INTO_GRAVEYARD}).
 */
public record PutExiledCardsIntoOwnersGraveyardEffect(CardSubject cards) implements CardEffect {

    public PutExiledCardsIntoOwnersGraveyardEffect {
        Objects.requireNonNull(cards, "cards");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The step names the cards an earlier step moved: " + cards);
        }
    }
}
