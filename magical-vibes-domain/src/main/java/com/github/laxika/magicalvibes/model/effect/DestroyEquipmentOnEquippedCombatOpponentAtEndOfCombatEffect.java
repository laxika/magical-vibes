package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/** Schedules equipment destruction for the referenced combat opponent at end of combat. */
public record DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect(
        boolean schedule, List<UUID> lastKnownEquipmentIds) implements CardEffect {
    public DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect() {
        this(true, List.of());
    }

    public DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect {
        lastKnownEquipmentIds = List.copyOf(lastKnownEquipmentIds);
    }
}
