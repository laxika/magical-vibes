package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each chosen target creature and has that creature's controller gain life equal to its
 * last-known power on the battlefield.
 */
public record ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect(
        com.github.laxika.magicalvibes.model.amount.DynamicAmount amount) implements RemovalEffect {

    public ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect() {
        this(new com.github.laxika.magicalvibes.model.amount.TargetPower());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
