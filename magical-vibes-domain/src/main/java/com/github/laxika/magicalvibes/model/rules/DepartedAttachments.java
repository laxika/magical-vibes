package com.github.laxika.magicalvibes.model.rules;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The Auras and Equipment attached to a permanent just before it left the battlefield, for abilities that look
 * back at it (CR 603.10a): "if it was enchanted", "for each Aura that was attached to it", "for each Aura you
 * controlled that was attached to it". Attachments that left in the same event count.
 *
 * @param auraCardIds           the card ids of the Auras attached to it
 * @param equipmentPermanentIds the permanent ids of the Equipment attached to it
 * @param auraControllerIds     the player who controlled each of {@code auraCardIds} then, by card id; an Aura
 *                              whose controller wasn't recorded is missing from it
 */
public record DepartedAttachments(List<UUID> auraCardIds, List<UUID> equipmentPermanentIds,
                                  Map<UUID, UUID> auraControllerIds) {

    /** Nothing was attached, or the move wasn't a departure. */
    public static final DepartedAttachments NONE = new DepartedAttachments(List.of(), List.of());

    public DepartedAttachments {
        auraCardIds = List.copyOf(auraCardIds);
        equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
        auraControllerIds = auraControllerIds == null ? Map.of() : Map.copyOf(auraControllerIds);
    }

    /** These attachments with no Aura controller recorded. */
    public DepartedAttachments(List<UUID> auraCardIds, List<UUID> equipmentPermanentIds) {
        this(auraCardIds, equipmentPermanentIds, Map.of());
    }

    /** How many of the Auras {@code playerId} controlled as the permanent left. */
    public int auraCountControlledBy(UUID playerId) {
        return playerId == null ? 0
                : (int) auraCardIds.stream().filter(id -> playerId.equals(auraControllerIds.get(id))).count();
    }
}
