package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost paid by putting a fixed number of cards from one graveyard on the bottoms of their owners'
 * libraries once for each payment.
 *
 * @param cardsPerPayment number of cards selected from one graveyard for each payment
 * @param payments number of separate payments required
 */
public record PutCardsFromSingleGraveyardOnBottomOfLibraryCost(int cardsPerPayment, int payments)
        implements CostEffect {

    public PutCardsFromSingleGraveyardOnBottomOfLibraryCost {
        if (cardsPerPayment <= 0) {
            throw new IllegalArgumentException("cardsPerPayment must be positive");
        }
        if (payments <= 0) {
            throw new IllegalArgumentException("payments must be positive");
        }
    }
}
