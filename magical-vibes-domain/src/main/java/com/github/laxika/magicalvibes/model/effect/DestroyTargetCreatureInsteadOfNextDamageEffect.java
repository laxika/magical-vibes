package com.github.laxika.magicalvibes.model.effect;

/** Replaces the next damage event to target creature this turn with destruction of that creature. */
public record DestroyTargetCreatureInsteadOfNextDamageEffect() implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
