package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * The Auras and Equipment attached to a permanent as it died, for death abilities that look back at them
 * (CR 603.10a), with the player who controlled each Aura then.
 */
public record DyingCreatureAttachmentSnapshot(List<UUID> auraCardIds, List<UUID> equipmentPermanentIds,
                                              Map<UUID, UUID> auraControllerIds) {

    public DyingCreatureAttachmentSnapshot {
        auraControllerIds = auraControllerIds == null ? Map.of() : Map.copyOf(auraControllerIds);
    }

    /** The attachments a departure event recorded. */
    public static DyingCreatureAttachmentSnapshot of(DepartedAttachments attachments) {
        return new DyingCreatureAttachmentSnapshot(attachments.auraCardIds(), attachments.equipmentPermanentIds(),
                attachments.auraControllerIds());
    }

    /** These attachments, for a departure event. */
    public DepartedAttachments toDepartedAttachments() {
        return new DepartedAttachments(auraCardIds, equipmentPermanentIds, auraControllerIds);
    }

    public static DyingCreatureAttachmentSnapshot capture(GameData gameData, UUID dyingPermanentId) {
        List<UUID> auraCardIds = new ArrayList<>();
        List<UUID> equipmentPermanentIds = new ArrayList<>();
        Map<UUID, UUID> auraControllerIds = new HashMap<>();

        gameData.forEachPermanent((playerId, permanent) -> addIfAttached(
                permanent, playerId, dyingPermanentId, auraCardIds, equipmentPermanentIds, auraControllerIds));

        gameData.simultaneousDyingPermanents.forEach((permanentId, permanent) -> {
            addIfAttached(permanent, gameData.simultaneousDyingPermanentControllers.get(permanentId),
                    dyingPermanentId, auraCardIds, equipmentPermanentIds, auraControllerIds);
        });

        return new DyingCreatureAttachmentSnapshot(List.copyOf(auraCardIds),
                List.copyOf(equipmentPermanentIds), auraControllerIds);
    }

    /**
     * Returns whether a dying creature's predicate holds when read as of its death (CR 603.10a), so
     * attachment conditions still hold when the Auras or Equipment left in the same event. Leaves that
     * are not attachment-dependent are decided by {@code matchesNow}.
     */
    public static boolean satisfiedAtDeath(DyingCreatureAttachmentSnapshot snapshot, PermanentPredicate predicate,
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

    private static void addIfAttached(Permanent permanent, UUID controllerId, UUID dyingPermanentId,
                                      List<UUID> auraCardIds, List<UUID> equipmentPermanentIds,
                                      Map<UUID, UUID> auraControllerIds) {
        if (!dyingPermanentId.equals(permanent.getAttachedTo())) {
            return;
        }
        if (permanent.getCard().isAura()) {
            if (auraCardIds.contains(permanent.getCard().getId())) {
                return;
            }
            auraCardIds.add(permanent.getCard().getId());
            if (controllerId != null) {
                auraControllerIds.put(permanent.getCard().getId(), controllerId);
            }
        } else if (permanent.getCard().getSubtypes().contains(
                com.github.laxika.magicalvibes.model.CardSubtype.EQUIPMENT)) {
            if (!equipmentPermanentIds.contains(permanent.getId())) {
                equipmentPermanentIds.add(permanent.getId());
            }
        }
    }
}
