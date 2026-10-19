package com.github.laxika.magicalvibes.model.effect;

/**
 * "Flip N coins. [perHeads] for each coin that comes up heads." (Ral Zarek −7).
 *
 * <p>All {@code coins} coins are flipped, then {@code perHeads} is dispatched once per heads, in
 * order, against the same stack entry. A multi-step payload must be a {@link SequenceEffect} of
 * synchronous steps, exactly as with {@link FlipCoinWinEffect}.
 *
 * <p>Only a contested flip ("for each flip you win", Goblin Traprunner) makes the controller win
 * the flip on heads and fire "whenever you win a coin flip" triggers. A flip that only cares about
 * heads or tails (CR 705.2, Ral Zarek) produces no winner, so {@code contested} is false.
 *
 * @param coins     how many coins to flip
 * @param perHeads  the effect to resolve once for each coin that comes up heads
 * @param contested true when each heads is a flip the controller wins (fires coin-flip-win triggers)
 */
public record FlipCoinsPerHeadsEffect(int coins, CardEffect perHeads, boolean contested) implements CardEffect {

    public FlipCoinsPerHeadsEffect(int coins, CardEffect perHeads) {
        this(coins, perHeads, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return perHeads == null ? TargetSpec.NONE : perHeads.targetSpec();
    }
}
