package com.github.laxika.magicalvibes.model.effect;

/** Exiles target creature and schedules its return under its owner's control at the next end step. */
public record ExileTargetCreatureAndReturnAtNextEndStepEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
