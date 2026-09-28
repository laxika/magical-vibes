package com.github.laxika.magicalvibes.model.effect;

/** Exiles a nonland card from a hand with time counters equal to its mana value. */
public record ExileNonlandCardFromHandWithManaValueTimeCountersEffect(boolean targetPlayer)
        implements CardEffect {

    public ExileNonlandCardFromHandWithManaValueTimeCountersEffect() {
        this(false);
    }

    public static ExileNonlandCardFromHandWithManaValueTimeCountersEffect forTargetPlayer() {
        return new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
