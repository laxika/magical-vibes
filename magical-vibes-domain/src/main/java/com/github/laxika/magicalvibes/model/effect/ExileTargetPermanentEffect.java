package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles the targeted permanent (also handles multi-target via targetIds).
 * Optionally creates a token for the exiled permanent's controller
 * (e.g. Crib Swap creates a 1/1 Shapeshifter for the target's controller).
 *
 * @param tokenForController if non-null, creates this token for each exiled permanent's controller
 */
public record ExileTargetPermanentEffect(CreateTokenEffect tokenForController,
                                         PermanentPredicate targetFilter)
        implements RemovalEffect {

    public ExileTargetPermanentEffect() {
        this(null, null);
    }

    public ExileTargetPermanentEffect(CreateTokenEffect tokenForController) {
        this(tokenForController, null);
    }

    public ExileTargetPermanentEffect(PermanentPredicate targetFilter) {
        this(null, targetFilter);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
