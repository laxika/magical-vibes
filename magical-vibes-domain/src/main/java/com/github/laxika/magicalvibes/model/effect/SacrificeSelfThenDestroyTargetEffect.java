package com.github.laxika.magicalvibes.model.effect;

/**
 * Sacrifices the source permanent; if it was successfully sacrificed ("if you do"), destroys the
 * permanent baked into the stack entry's {@code targetId}. Pair with a {@link MayEffect} wrapper for
 * "you may sacrifice this creature. If you do, destroy target creature" (Wasp of the Bitter End):
 * the creature target is chosen as the trigger goes on the stack (CR 603.3d); the may/sacrifice
 * choice waits for resolution (CR 603.5).
 */
public record SacrificeSelfThenDestroyTargetEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
