package com.github.laxika.magicalvibes.model.effect;

public record ExchangeTargetPlayersLifeTotalsEffect() implements CardEffect {
    @Override public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), false, null, false, 2);
    }
}
