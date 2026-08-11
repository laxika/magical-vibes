package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * Dismantle: destroy target artifact, then choose whether to put that artifact's total number of
 * counters onto an artifact the controller controls as +1/+1 counters or charge counters.
 */
public record DismantleEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
