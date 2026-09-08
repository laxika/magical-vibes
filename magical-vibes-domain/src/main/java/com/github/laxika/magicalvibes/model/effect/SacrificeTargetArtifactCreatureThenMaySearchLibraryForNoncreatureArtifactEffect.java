package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * The controller of the target artifact creature sacrifices it, then may search that player's
 * library for a noncreature artifact card and put it onto the battlefield.
 */
public record SacrificeTargetArtifactCreatureThenMaySearchLibraryForNoncreatureArtifactEffect()
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), new PermanentIsArtifactPredicate());
    }
}
