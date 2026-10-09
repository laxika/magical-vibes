package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * found, offers that card to be cast without paying its mana cost, and puts the other revealed
 * cards on the bottom of the library in a random order. When {@code shuffleLibrary} is true, the
 * whole library is shuffled after the choice instead. An optional second predicate can restrict
 * whether the found card is offered without changing which card stops the reveal. When
 * {@code exileFoundCard} is true, the matching card is exiled before the casting choice and stays
 * there if the choice is declined.
 */
public record RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate,
                                                                       boolean shuffleLibrary,
                                                                       CardPredicate castPredicate,
                                                                       boolean exileFoundCard)
        implements CardEffect {

    public RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate,
                                                                    boolean shuffleLibrary,
                                                                    CardPredicate castPredicate) {
        this(predicate, shuffleLibrary, castPredicate, false);
    }

    public RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate,
                                                                    boolean shuffleLibrary) {
        this(predicate, shuffleLibrary, null, false);
    }

    public RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate) {
        this(predicate, false, null, false);
    }
}
