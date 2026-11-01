package com.github.laxika.magicalvibes.model.rules;

import java.util.List;
import java.util.UUID;

/**
 * The Auras and Equipment attached to a permanent just before it left the battlefield, for abilities that look
 * back at it (CR 603.10a): "if it was enchanted", "for each Aura that was attached to it". Attachments that left
 * in the same event count.
 *
 * @param auraCardIds           the card ids of the Auras attached to it
 * @param equipmentPermanentIds the permanent ids of the Equipment attached to it
 */
public record DepartedAttachments(List<UUID> auraCardIds, List<UUID> equipmentPermanentIds) {

    /** Nothing was attached, or the move wasn't a departure. */
    public static final DepartedAttachments NONE = new DepartedAttachments(List.of(), List.of());

    public DepartedAttachments {
        auraCardIds = List.copyOf(auraCardIds);
        equipmentPermanentIds = List.copyOf(equipmentPermanentIds);
    }
}
