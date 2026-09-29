package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/** Restricts the current additional combat phase to the chosen creature permanents. */
public record OnlyTargetCreaturesCanAttackThisCombatEffect(Set<UUID> permanentIds) implements CardEffect {

    public OnlyTargetCreaturesCanAttackThisCombatEffect {
        permanentIds = Set.copyOf(permanentIds);
    }
}
