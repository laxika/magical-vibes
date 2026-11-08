package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exiles the targeted permanent (also handles multi-target via targetIds).
 * Optionally creates a token for the exiled permanent's controller
 * (e.g. Crib Swap creates a 1/1 Shapeshifter for the target's controller).
 *
 * @param tokenForController if non-null, creates this token for each exiled permanent's controller
 * @param targetFilter optional predicate narrowing the permanent target
 * @param triggeringPermanent whether it exiles the stack entry's triggering permanent instead of a target ("exile that
 *                           creature" of a damage trigger: {@link #ofTriggeringPermanent()}); it then targets nothing
 */
public record ExileTargetPermanentEffect(CreateTokenEffect tokenForController,
                                         PermanentPredicate targetFilter,
                                         boolean triggeringPermanent)
        implements RemovalEffect {

    public ExileTargetPermanentEffect() {
        this(null, null);
    }

    public ExileTargetPermanentEffect(CreateTokenEffect tokenForController, PermanentPredicate targetFilter) {
        this(tokenForController, targetFilter, false);
    }

    /**
     * "Exile that creature": the stack entry's triggering permanent (Pit Spawn, Sword of Kaldra), exiled only if it is
     * still on the battlefield.
     */
    public static ExileTargetPermanentEffect ofTriggeringPermanent() {
        return new ExileTargetPermanentEffect(null, null, true);
    }

    public ExileTargetPermanentEffect(CreateTokenEffect tokenForController) {
        this(tokenForController, null);
    }

    public ExileTargetPermanentEffect(PermanentPredicate targetFilter) {
        this(null, targetFilter);
    }

    @Override
    public TargetSpec targetSpec() {
        if (triggeringPermanent) {
            return TargetSpec.NONE;
        }
        return targetFilter == null
                ? TargetSpec.harmful(TargetPredicates.permanent())
                : TargetSpec.harmful(TargetPredicates.permanent(), targetFilter);
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
