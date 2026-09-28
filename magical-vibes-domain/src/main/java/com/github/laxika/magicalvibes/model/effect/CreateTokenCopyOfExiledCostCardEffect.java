package com.github.laxika.magicalvibes.model.effect;

/**
 * Creates a token that's a copy of the card exiled as part of an {@link ExileCardFromGraveyardCost}
 * with {@code imprintOnSource = true}. The exiled card is tracked via the source permanent's
 * imprinted card reference, set during cost payment.
 */
public record CreateTokenCopyOfExiledCostCardEffect(int count) implements CardEffect {

    public CreateTokenCopyOfExiledCostCardEffect() {
        this(1);
    }

    public CreateTokenCopyOfExiledCostCardEffect {
        if (count < 1) {
            throw new IllegalArgumentException("Token copy count must be positive");
        }
    }
}
