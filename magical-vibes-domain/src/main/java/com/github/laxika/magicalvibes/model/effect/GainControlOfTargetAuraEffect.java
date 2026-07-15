package com.github.laxika.magicalvibes.model.effect;

public record GainControlOfTargetAuraEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // The kept validator additionally requires the target to be an Aura that is attached — an
        // attachment-state check the PERMANENT category cannot express.
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
