package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles a target permanent until the source permanent leaves the battlefield, then returns
 * every other card exiled with the source to the battlefield under its owner's control.
 *
 * @param targetPredicate optional restriction on the permanent target
 */
public record ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect(
        PermanentPredicate targetPredicate) implements CardEffect {

    public ExileTargetPermanentUntilSourceLeavesAndReturnOthersEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPredicate == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetPredicate);
    }
}
