package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the target permanent, then makes the source a copy of the target's copiable values until
 * end of turn. The target is returned under its owner's control at the beginning of the next end
 * step.
 */
public record ExileTargetPermanentAndBecomeCopyUntilEndOfTurnEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
