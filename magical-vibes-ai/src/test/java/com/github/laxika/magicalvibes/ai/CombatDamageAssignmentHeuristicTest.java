package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDamageAssignmentHeuristicTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameData gameData;

    @Test
    @DisplayName("Double block: kills the higher-power blocker first (Geth vs Machine + Souleater)")
    void prioritizesMostThreateningKillableBlocker() {
        UUID machineId = UUID.randomUUID();
        UUID souleaterId = UUID.randomUUID();
        Permanent machinePerm = mock(Permanent.class);
        Permanent souleaterPerm = mock(Permanent.class);

        // Presentation order puts the weaker blocker first — the old heuristic killed it.
        CombatDamageTarget souleaterTarget = target(souleaterId, "Blinding Souleater", 3);
        CombatDamageTarget machineTarget = target(machineId, "Diabolic Machine", 4);

        when(gameQueryService.findPermanentById(gameData, machineId)).thenReturn(machinePerm);
        when(gameQueryService.findPermanentById(gameData, souleaterId)).thenReturn(souleaterPerm);
        when(gameQueryService.getEffectivePower(gameData, machinePerm)).thenReturn(4);
        when(gameQueryService.getEffectivePower(gameData, souleaterPerm)).thenReturn(1);
        when(gameQueryService.hasKeyword(eq(gameData), eq(machinePerm), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
        when(gameQueryService.hasKeyword(eq(gameData), eq(souleaterPerm), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 7, UUID.randomUUID(), "Geth, Lord of the Vault", 5,
                List.of(souleaterTarget, machineTarget), false, false, false);

        Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                interaction, gameData, gameQueryService);

        assertThat(assignments).containsEntry(machineId, 4);
        assertThat(assignments).containsEntry(souleaterId, 1);
        assertThat(assignments.values().stream().mapToInt(Integer::intValue).sum()).isEqualTo(5);
    }

    @Test
    @DisplayName("Deathtouch: 1 damage is lethal; remaining kills next highest threat")
    void deathtouchSpreadsLethalByThreat() {
        UUID bigId = UUID.randomUUID();
        UUID smallId = UUID.randomUUID();
        Permanent big = mock(Permanent.class);
        Permanent small = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, bigId)).thenReturn(big);
        when(gameQueryService.findPermanentById(gameData, smallId)).thenReturn(small);
        when(gameQueryService.getEffectivePower(gameData, big)).thenReturn(5);
        when(gameQueryService.getEffectivePower(gameData, small)).thenReturn(2);
        when(gameQueryService.hasKeyword(eq(gameData), eq(big), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
        when(gameQueryService.hasKeyword(eq(gameData), eq(small), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 0, UUID.randomUUID(), "Deathtouch Attacker", 3,
                List.of(target(smallId, "Small", 2), target(bigId, "Big", 6)),
                false, true, false);

        Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                interaction, gameData, gameQueryService);

        // 1 lethal each, then excess 1 piles on the higher threat.
        assertThat(assignments).containsEntry(bigId, 2);
        assertThat(assignments).containsEntry(smallId, 1);
    }

    @Test
    @DisplayName("Trample: lethal to all blockers, excess to player")
    void trampleSendsExcessToPlayerAfterLethal() {
        UUID blockerId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Permanent blocker = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, blockerId)).thenReturn(blocker);
        when(gameQueryService.hasKeyword(eq(gameData), eq(blocker), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 0, UUID.randomUUID(), "Trampler", 5,
                List.of(target(blockerId, "Blocker", 2),
                        new CombatDamageTarget(playerId, "Defender", 0, 0, true)),
                true, false, false);

        Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                interaction, gameData, gameQueryService);

        assertThat(assignments).containsEntry(blockerId, 2);
        assertThat(assignments).containsEntry(playerId, 3);
    }

    @Test
    @DisplayName("Single-recipient: prefers the most threatening killable creature")
    void singleRecipientPrefersTopThreatKill() {
        UUID wallId = UUID.randomUUID();
        UUID bearId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Permanent wall = mock(Permanent.class);
        Permanent bear = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, wallId)).thenReturn(wall);
        when(gameQueryService.findPermanentById(gameData, bearId)).thenReturn(bear);
        when(gameQueryService.getEffectivePower(gameData, bear)).thenReturn(2);
        when(gameQueryService.hasKeyword(eq(gameData), eq(wall), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);
        when(gameQueryService.hasKeyword(eq(gameData), eq(bear), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 0, UUID.randomUUID(), "Cunning Giant", 3,
                List.of(target(wallId, "Wall", 7), target(bearId, "Bear", 2),
                        new CombatDamageTarget(playerId, "Defender", 0, 0, true)),
                false, false, true);

        Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                interaction, gameData, gameQueryService);

        assertThat(assignments).containsExactly(Map.entry(bearId, 3));
    }

    @Test
    @DisplayName("Skips indestructible when choosing kill targets")
    void skipsIndestructibleForKillPriority() {
        UUID indestructibleId = UUID.randomUUID();
        UUID fragileId = UUID.randomUUID();
        Permanent indestructible = mock(Permanent.class);
        Permanent fragile = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, indestructibleId)).thenReturn(indestructible);
        when(gameQueryService.findPermanentById(gameData, fragileId)).thenReturn(fragile);
        when(gameQueryService.getEffectivePower(gameData, indestructible)).thenReturn(6);
        when(gameQueryService.getEffectivePower(gameData, fragile)).thenReturn(2);
        when(gameQueryService.hasKeyword(eq(gameData), eq(indestructible), eq(Keyword.INDESTRUCTIBLE))).thenReturn(true);
        when(gameQueryService.hasKeyword(eq(gameData), eq(fragile), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 0, UUID.randomUUID(), "Attacker", 4,
                List.of(target(indestructibleId, "Indestructible", 4), target(fragileId, "Fragile", 2)),
                false, false, false);

        Map<UUID, Integer> assignments = CombatDamageAssignmentHeuristic.assign(
                interaction, gameData, gameQueryService);

        assertThat(assignments).containsEntry(fragileId, 2);
        assertThat(assignments.getOrDefault(indestructibleId, 0)).isEqualTo(2);
    }

    private static CombatDamageTarget target(UUID id, String name, int toughness) {
        return new CombatDamageTarget(id, name, toughness, 0, false);
    }
}
