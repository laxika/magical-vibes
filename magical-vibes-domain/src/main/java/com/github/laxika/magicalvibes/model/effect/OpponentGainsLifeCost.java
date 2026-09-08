package com.github.laxika.magicalvibes.model.effect;

/**
 * Cost effect paid by having the source controller's opponent gain life (Wall of Shards'
 * cumulative upkeep). The cost is always paid by the opponent gaining the life, while the
 * source controller chooses whether to pay the optional cumulative-upkeep cost.
 *
 * @param amount how much life the opponent gains
 */
public record OpponentGainsLifeCost(int amount) implements CostEffect {

    public OpponentGainsLifeCost {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }
    }
}
