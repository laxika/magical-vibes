package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;

import java.util.UUID;

/** Attaches a selected Equipment permanent to a target creature controlled by the ability controller. */
public record AttachSelectedEquipmentToTargetCreatureEffect(UUID equipmentPermanentId)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(
                TargetPredicates.creature(),
                new PermanentControlledBySourceControllerPredicate());
    }
}
