package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches a target Aura or Equipment that is attached to a creature to a target creature.
 */
public record AttachTargetAuraOrEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
