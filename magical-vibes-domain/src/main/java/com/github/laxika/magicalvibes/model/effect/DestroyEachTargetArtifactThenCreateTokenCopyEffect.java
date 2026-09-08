package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * Destroys each targeted artifact and, when the resolving spell's X value is at least five,
 * creates a hasty token copy of every artifact actually destroyed. The copies are exiled at the
 * beginning of the next end step.
 */
public record DestroyEachTargetArtifactThenCreateTokenCopyEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
