package com.github.laxika.magicalvibes.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Queue-only state for Raging River's defending-player pile choices and attacker labels. */
public record PendingRagingRiver(
        UUID controllerId,
        List<UUID> attackerIds,
        Map<UUID, UUID> defendingPlayerByAttackerId,
        List<UUID> defendingPlayerIds,
        Map<UUID, List<UUID>> groundCreatureIdsByDefendingPlayer,
        Map<UUID, List<UUID>> pile1IdsByDefendingPlayer,
        Map<UUID, List<UUID>> pile2IdsByDefendingPlayer,
        int nextDefendingPlayerIndex,
        int nextAttackerIndex
) implements PendingInteraction {

    public PendingRagingRiver {
        attackerIds = List.copyOf(attackerIds);
        defendingPlayerByAttackerId = Map.copyOf(defendingPlayerByAttackerId);
        defendingPlayerIds = List.copyOf(defendingPlayerIds);
        groundCreatureIdsByDefendingPlayer = copyListMap(groundCreatureIdsByDefendingPlayer);
        pile1IdsByDefendingPlayer = copyListMap(pile1IdsByDefendingPlayer);
        pile2IdsByDefendingPlayer = copyListMap(pile2IdsByDefendingPlayer);
    }

    private static Map<UUID, List<UUID>> copyListMap(Map<UUID, List<UUID>> source) {
        Map<UUID, List<UUID>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Map.copyOf(copy);
    }
}
