package com.github.laxika.magicalvibes.model.effect;

/** Attaches a target Equipment to a target creature. */
public record AttachTargetEquipmentToTargetCreatureEffect(boolean equipmentFirst) implements CardEffect {

    public AttachTargetEquipmentToTargetCreatureEffect() {
        this(true);
    }

    public static AttachTargetEquipmentToTargetCreatureEffect creatureFirst() {
        return new AttachTargetEquipmentToTargetCreatureEffect(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
