package com.github.laxika.magicalvibes.model.effect;

/** Exiles target creature and schedules its return at the next end step unless its controller pays. */
public record ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
