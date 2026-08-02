package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles each chosen target creature and has that creature's controller gain life equal to its
 * last-known power on the battlefield.
 */
public record ExileTargetCreaturesAndControllersGainLifeEqualToPowerEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
