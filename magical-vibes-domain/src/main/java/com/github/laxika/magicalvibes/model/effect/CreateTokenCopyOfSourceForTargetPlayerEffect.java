package com.github.laxika.magicalvibes.model.effect;

/** Creates a token copy of the source permanent under the targeted player's control. */
public record CreateTokenCopyOfSourceForTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
