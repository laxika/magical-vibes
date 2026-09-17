package com.github.laxika.magicalvibes.model.effect;

import java.util.Set;
import java.util.UUID;

/** Beginning-of-combat payload for Illusionist's Gambit's inserted combat phase. */
public record IllusionistsGambitAdditionalCombatEffect(
        Set<UUID> attackerIds,
        UUID protectedPlayerId) implements CardEffect {

    public IllusionistsGambitAdditionalCombatEffect {
        attackerIds = Set.copyOf(attackerIds);
    }
}
