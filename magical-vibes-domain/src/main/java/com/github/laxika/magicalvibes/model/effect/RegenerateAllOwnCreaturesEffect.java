package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Regenerates every creature its controller controls that matches {@code filter} (all of them when the
 * filter is {@code null}).
 *
 * @param filter optional restriction on which of the controller's creatures are regenerated
 * @param excludeTargets when {@code true}, creatures that are already targets of this spell/ability are
 *        skipped, so an "also regenerate each <em>other</em> creature you control" rider never hands the
 *        targeted creature a second regeneration shield (Dark Dabbling)
 */
public record RegenerateAllOwnCreaturesEffect(PermanentPredicate filter, boolean excludeTargets) implements CardEffect {

    public RegenerateAllOwnCreaturesEffect() {
        this(null, false);
    }

    public RegenerateAllOwnCreaturesEffect(PermanentPredicate filter) {
        this(filter, false);
    }
}
