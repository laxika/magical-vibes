package com.github.laxika.magicalvibes.model.effect;

/** Exiles a target permanent, tracks it with this source, and lets its owner play it while exiled. */
public record ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }
}
