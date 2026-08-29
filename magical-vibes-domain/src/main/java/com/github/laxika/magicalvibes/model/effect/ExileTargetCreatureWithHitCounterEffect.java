package com.github.laxika.magicalvibes.model.effect;

/** Exiles the targeted creature, marks it with a hit counter, and applies Etrata's loss check. */
public record ExileTargetCreatureWithHitCounterEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
