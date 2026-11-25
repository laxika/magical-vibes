package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "Exile it" / "exile that card" as a cost paid as an effect resolves ("you may pay {1} and exile it. If you do, …";
 * CR 118.12): payable while the subject names its card where it is expected to be. A card that left that zone is a new
 * object (CR 400.7), so the cost can't be paid (Timothar, Baron of Bats' ruling). The cards exiled are the resolution's
 * {@code StepKind.EXILE} step, so "the exiled card" after it is {@code Subjects.cardsThisWay(StepKind.EXILE)}.
 *
 * @param cards the cards to exile ({@code Subjects.triggeringCards()}: "it", the card a trigger is about)
 */
public record ExileCardsCost(CardSubject cards) implements CostEffect {

    public ExileCardsCost {
        Objects.requireNonNull(cards, "cards");
    }
}
