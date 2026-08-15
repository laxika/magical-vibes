package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the targeted permanent, then creates a token copy of the exiled permanent under the
 * spell's controller. The token is exiled at the beginning of the next end step.
 */
public record ExileTargetPermanentAndCreateTokenCopyEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
