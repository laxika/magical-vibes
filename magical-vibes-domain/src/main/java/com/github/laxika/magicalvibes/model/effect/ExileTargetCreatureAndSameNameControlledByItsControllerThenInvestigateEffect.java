package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles target creature and all other creatures its controller controls with the same name,
 * then that player investigates for each nontoken creature exiled this way.
 */
public record ExileTargetCreatureAndSameNameControlledByItsControllerThenInvestigateEffect()
        implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
