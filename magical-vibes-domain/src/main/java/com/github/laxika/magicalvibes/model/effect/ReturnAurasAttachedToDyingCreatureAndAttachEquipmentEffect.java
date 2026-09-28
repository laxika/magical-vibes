package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Returns captured Auras from the controller's graveyard and reattaches captured Equipment. */
public record ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect(
        List<UUID> auraCardIds,
        List<UUID> equipmentPermanentIds
) implements CardEffect, DyingCreatureAttachmentsAwareEffect {

    public ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect() {
        this(List.of(), List.of());
    }

    public ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect {
        auraCardIds = List.copyOf(auraCardIds);
        equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public CardEffect boundToDyingCreatureAttachments(List<UUID> auraCardIds,
                                                       List<UUID> equipmentPermanentIds) {
        return new ReturnAurasAttachedToDyingCreatureAndAttachEquipmentEffect(
                auraCardIds, equipmentPermanentIds);
    }
}
