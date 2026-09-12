package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activation cost that exiles one or more other artifacts the activating player controls.
 * The mana values of the exiled artifacts are added together for the ability's X value.
 *
 * @param minimumCount the minimum number of artifacts to exile
 * @param allowsAdditionalArtifacts whether more than {@code minimumCount} artifacts may be exiled
 * @param filter optional additional permanent filter
 * @param trackWithSource whether the exiled permanents are tracked with the source permanent
 */
public record ExileArtifactsWithTotalManaValueCost(int minimumCount,
                                                    boolean allowsAdditionalArtifacts,
                                                    PermanentPredicate filter,
                                                    boolean trackWithSource) implements CostEffect {

    public ExileArtifactsWithTotalManaValueCost() {
        this(1, true, null, false);
    }
}
