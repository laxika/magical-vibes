package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect paid by having an opponent of the source controller gain life (Wall of Shards'
 * cumulative upkeep). The cost is always paid by an opponent gaining the life, while the
 * source controller chooses whether to pay the optional cumulative-upkeep cost. With several
 * payments (one per age counter) each is its own choice of opponent (the Wall of Shards ruling).
 *
 * @param amount   how much life the opponents gain in all
 * @param payments how many separate payments the amount is split into, each to an opponent the payer chooses
 */
public record OpponentGainsLifeCost(int amount, int payments) implements CostEffect {

    public OpponentGainsLifeCost {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
        if (payments < 1 || amount % payments != 0) {
            throw new IllegalArgumentException("amount must split evenly into at least one payment");
        }
    }

    public OpponentGainsLifeCost(int amount) {
        this(amount, 1);
    }

    /** The life one payment gives. */
    public int amountPerPayment() {
        return amount / payments;
    }
}
