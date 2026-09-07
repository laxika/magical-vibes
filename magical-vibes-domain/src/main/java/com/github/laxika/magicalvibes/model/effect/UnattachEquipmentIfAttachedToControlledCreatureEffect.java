package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Unattaches a remembered Equipment when it is still attached to a creature controlled by the trigger controller. */
public record UnattachEquipmentIfAttachedToControlledCreatureEffect(UUID equipmentId) implements CardEffect {
}
