package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/** Destroys each targeted artifact and conjures a modified duplicate of each nontoken artifact destroyed. */
public record DestroyTargetArtifactsThenConjurePerpetualCopiesIntoHandEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
