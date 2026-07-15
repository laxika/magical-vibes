package com.github.laxika.magicalvibes.model.effect;

public record TargetPlayerGainsControlOfSourceCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // The kept validator enforces requireTargetPlayer, which the no-op PLAYER category cannot
        // reproduce; the spec only carries the derived canTargetPlayer boolean.
        return TargetSpec.benign(TargetCategory.PLAYER);
    }
}
