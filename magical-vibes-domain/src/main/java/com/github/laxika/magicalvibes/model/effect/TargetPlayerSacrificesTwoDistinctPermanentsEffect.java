package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Target player sacrifices a [first] and a [second]" (Structural Collapse's artifact and land).
 * The targeted player chooses one permanent matching each filter, all choices are made before
 * anything is sacrificed, and the chosen permanents are sacrificed at the same time. One permanent
 * can satisfy both filters (an artifact land) only when the player has no way to sacrifice two
 * different permanents, so a single permanent is never counted twice and is sacrificed once.
 *
 * @param first  filter for the first sacrificed permanent
 * @param second filter for the second sacrificed permanent
 */
public record TargetPlayerSacrificesTwoDistinctPermanentsEffect(PermanentPredicate first,
                                                                PermanentPredicate second)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
