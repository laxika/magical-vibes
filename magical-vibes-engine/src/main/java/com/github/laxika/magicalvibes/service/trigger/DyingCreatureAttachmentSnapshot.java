package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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

    /**
     * Returns whether a dying creature's predicate holds when read as of its death (CR 603.10a), so
     * attachment conditions still hold when the Auras or Equipment left in the same event. Leaves that
     * are not attachment-dependent are decided by {@code matchesNow}.
     */
    static boolean satisfiedAtDeath(DyingCreatureAttachmentSnapshot snapshot, PermanentPredicate predicate,
                                    Predicate<PermanentPredicate> matchesNow) {
        if (matchesNow.test(predicate)) {
            return true;
        }
        if (snapshot == null) {
            return false;
        }
        if (predicate instanceof PermanentIsEnchantedPredicate) {
            return !snapshot.auraCardIds().isEmpty();
        }
        if (predicate instanceof PermanentIsEquippedPredicate) {
            return !snapshot.equipmentPermanentIds().isEmpty();
        }
        if (predicate instanceof PermanentAnyOfPredicate anyOf) {
            return anyOf.predicates().stream().anyMatch(p -> satisfiedAtDeath(snapshot, p, matchesNow));
        }
        if (predicate instanceof PermanentAllOfPredicate allOf) {
            return allOf.predicates().stream().allMatch(p -> satisfiedAtDeath(snapshot, p, matchesNow));
        }
        return false;
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
