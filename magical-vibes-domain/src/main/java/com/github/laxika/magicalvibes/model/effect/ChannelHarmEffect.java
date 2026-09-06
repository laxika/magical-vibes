package com.github.laxika.magicalvibes.model.effect;

/** Installs Channel Harm's turn-long prevention shield. */
public record ChannelHarmEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
