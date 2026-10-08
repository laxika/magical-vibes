package com.github.laxika.magicalvibes.model.effect;

/** Installs Channel Harm's prevention shield, or carries its optional damage for a prevention event. */
public record ChannelHarmEffect(Integer preventedDamage) implements CardEffect {

    public ChannelHarmEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return preventedDamage == null ? TargetSpec.harmful(TargetPredicates.creature()) : TargetSpec.NONE;
    }
}
