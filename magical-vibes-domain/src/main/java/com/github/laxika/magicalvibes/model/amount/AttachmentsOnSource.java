package com.github.laxika.magicalvibes.model.amount;

/** The number of Auras and/or Equipment attached to the source permanent. */
public record AttachmentsOnSource(boolean countAuras, boolean countEquipment) implements DynamicAmount {
}
