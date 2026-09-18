package com.github.laxika.magicalvibes.model.effect;

/** Attaches the target Equipment to the source creature. */
public record AttachTargetEquipmentToSourceEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
