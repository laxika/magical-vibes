package com.github.laxika.magicalvibes.model.effect;

/**
 * "Flip N coins. [perHeads] for each coin that comes up heads." (Ral Zarek −7).
 *
 * <p>All {@code coins} coins are flipped, then {@code perHeads} is dispatched once per heads, in
 * order, against the same stack entry. A multi-step payload must be a {@link SequenceEffect} of
 * synchronous steps, exactly as with {@link FlipCoinWinEffect}.
 *
 * @param coins    how many coins to flip
 * @param perHeads the effect to resolve once for each coin that comes up heads
 */
public record FlipCoinsPerHeadsEffect(int coins, CardEffect perHeads) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return perHeads == null ? TargetSpec.NONE : perHeads.targetSpec();
    }
}
