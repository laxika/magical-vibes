package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys each chosen target creature and puts two +1/+1 counters on the source permanent if a
 * creature controlled by the effect's controller was actually destroyed this way.
 */
public record DestroyTargetCreaturesThenPutCountersOnSourceIfOwnDestroyedEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
