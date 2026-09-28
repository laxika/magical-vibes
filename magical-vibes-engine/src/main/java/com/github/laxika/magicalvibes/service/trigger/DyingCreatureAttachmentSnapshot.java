package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

record DyingCreatureAttachmentSnapshot(List<UUID> auraCardIds, List<UUID> equipmentPermanentIds) {

    static DyingCreatureAttachmentSnapshot capture(GameData gameData, UUID dyingPermanentId) {
        List<UUID> auraCardIds = new ArrayList<>();
        List<UUID> equipmentPermanentIds = new ArrayList<>();

        gameData.forEachPermanent((playerId, permanent) -> addIfAttached(
                permanent, dyingPermanentId, auraCardIds, equipmentPermanentIds));

        gameData.simultaneousDyingPermanents.forEach((permanentId, permanent) -> {
            addIfAttached(permanent, dyingPermanentId, auraCardIds, equipmentPermanentIds);
        });

        return new DyingCreatureAttachmentSnapshot(List.copyOf(auraCardIds),
                List.copyOf(equipmentPermanentIds));
    }

    private static void addIfAttached(Permanent permanent, UUID dyingPermanentId,
                                      List<UUID> auraCardIds, List<UUID> equipmentPermanentIds) {
        if (!dyingPermanentId.equals(permanent.getAttachedTo())) {
            return;
        }
        if (permanent.getCard().isAura()) {
            auraCardIds.add(permanent.getCard().getId());
        } else if (permanent.getCard().getSubtypes().contains(
                com.github.laxika.magicalvibes.model.CardSubtype.EQUIPMENT)) {
            equipmentPermanentIds.add(permanent.getId());
        }
    }
}
