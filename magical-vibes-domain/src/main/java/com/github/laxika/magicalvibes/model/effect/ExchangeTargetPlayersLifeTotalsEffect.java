package com.github.laxika.magicalvibes.model.effect;

public record ExchangeTargetPlayersLifeTotalsEffect(boolean controllerAndTarget) implements CardEffect {
    public ExchangeTargetPlayersLifeTotalsEffect() {
        this(false);
    }

    public static ExchangeTargetPlayersLifeTotalsEffect forControllerAndTarget() {
        return new ExchangeTargetPlayersLifeTotalsEffect(true);
    }

    @Override public TargetSpec targetSpec() {
        return new TargetSpec(TargetPredicates.player(), false, null, false, controllerAndTarget ? 1 : 2);
    }
}
