package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Unattaches the remembered Equipment if it is still attached. */
public record UnattachEquipmentEffect(UUID equipmentId) implements CardEffect {

    /** Uses the activated ability's source permanent as the Equipment to unattach. */
    public static UnattachEquipmentEffect source() {
        return new UnattachEquipmentEffect(null);
    }
}
