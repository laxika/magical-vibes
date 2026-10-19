package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Reveals cards from the top of the controller's library until a card matching the predicate is
 * found, offers that card to be cast without paying its mana cost, and puts the other revealed
 * cards on the bottom of the library in a random order. When {@code shuffleLibrary} is true, the
 * whole library is shuffled after the choice instead. An optional second predicate can restrict
 * whether the found card is offered without changing which card stops the reveal. When
 * {@code exileFoundCard} is true, the matching card is exiled before the casting choice and stays
 * there if the choice is declined. When {@code exileRevealedCards} is true, every card is exiled rather
 * than revealed ("exile cards from the top of your library until you exile a nonland card", Ryan
 * Sinclair): the found card is cast from exile, and the exiled cards not cast this way go to the bottom
 * of the library in a random order.
 */
public record RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate,
                                                                       boolean shuffleLibrary,
                                                                       CardPredicate castPredicate,
                                                                       boolean exileFoundCard,
                                                                       boolean exileRevealedCards)
        implements CardEffect {

    public RevealUntilCardPredicateMayCastWithoutPayingManaEffect(CardPredicate predicate,
                                                                    boolean shuffleLibrary,
                                                                    CardPredicate castPredicate,
                                                                    boolean exileFoundCard) {
        this(predicate, shuffleLibrary, castPredicate, exileFoundCard, false);
    }

    /** Exiles every card down to the first match, which is cast from exile if the cast predicate allows. */
    public static RevealUntilCardPredicateMayCastWithoutPayingManaEffect exilingUntil(
            CardPredicate predicate, CardPredicate castPredicate) {
        return new RevealUntilCardPredicateMayCastWithoutPayingManaEffect(
                predicate, false, castPredicate, false, true);
    }

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
