package com.github.laxika.magicalvibes.model.effect;

public record ExchangeTargetPlayersLifeTotalsEffect() implements CardEffect {
    @Override public boolean canTargetPlayer() { return true; }
    @Override public int requiredPlayerTargetCount() { return 2; }
}
