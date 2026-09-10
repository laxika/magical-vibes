package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

/** Attaches the Equipment remembered as the reflexive ability's source to a target creature its ability controller controls. */
public record AttachCreatedEquipmentToTargetCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
