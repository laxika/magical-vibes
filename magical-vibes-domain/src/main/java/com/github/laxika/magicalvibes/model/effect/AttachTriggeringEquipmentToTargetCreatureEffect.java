package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches the Equipment that caused the current enter trigger to its target creature.
 */
public record AttachTriggeringEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
