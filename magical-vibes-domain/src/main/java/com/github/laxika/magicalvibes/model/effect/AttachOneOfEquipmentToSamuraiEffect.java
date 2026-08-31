package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Attaches one of the listed Equipment permanents to a Samurai controlled by the ability controller. */
public record AttachOneOfEquipmentToSamuraiEffect(List<UUID> equipmentPermanentIds)
        implements CardEffect, LibrarySelectionFollowUp {

    public AttachOneOfEquipmentToSamuraiEffect() {
        this(List.of());
    }

    public AttachOneOfEquipmentToSamuraiEffect {
        equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
    }

    @Override
    public CardEffect createEffect(List<UUID> selectedPermanentIds) {
        return new AttachOneOfEquipmentToSamuraiEffect(selectedPermanentIds);
    }

    @Override
    public String prompt() {
        return "Attach one of those Equipment to a Samurai you control?";
    }
}
