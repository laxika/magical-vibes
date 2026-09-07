package com.github.laxika.magicalvibes.model.effect;

/** Lets the controller choose any number of their Equipment to attach to the target creature. */
public record AttachAnyNumberOfControlledEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
