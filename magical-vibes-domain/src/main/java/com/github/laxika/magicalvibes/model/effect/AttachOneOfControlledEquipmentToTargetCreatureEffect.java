package com.github.laxika.magicalvibes.model.effect;

/** Chooses one Equipment controlled by the effect's controller and attaches it to the target creature. */
public record AttachOneOfControlledEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
