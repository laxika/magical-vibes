package com.github.laxika.magicalvibes.ai.interaction;

import com.github.laxika.magicalvibes.ai.AiGameActions;
import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDamageAssignmentAiStrategyTest {

    private final CombatDamageAssignmentAiStrategy strategy = new CombatDamageAssignmentAiStrategy();

    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameData gameData;
    @Mock
    private AiGameActions gameActions;
    @Mock
    private Connection selfConnection;

    @Test
    @DisplayName("handledType is CombatDamageAssignment")
    void handledType() {
        assertThat(strategy.handledType()).isEqualTo(PendingInteraction.CombatDamageAssignment.class);
    }

    @Test
    @DisplayName("Wrong deciding player: does not send an assignment")
    void ignoresWrongPlayer() throws Exception {
        UUID aiPlayerId = UUID.randomUUID();
        UUID otherPlayerId = UUID.randomUUID();
        var interaction = new PendingInteraction.CombatDamageAssignment(
                otherPlayerId, 7, UUID.randomUUID(), "Geth, Lord of the Vault", 5,
                List.of(target(UUID.randomUUID(), "Blocker", 3)), false, false, false);

        strategy.answer(interaction, context(aiPlayerId));

        verify(gameActions, never()).handleCombatDamageAssigned(eq(selfConnection),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Sends heuristic assignment with string UUID keys for the Geth double-block case")
    void sendsThreatPrioritizedAssignment() throws Exception {
        UUID aiPlayerId = UUID.randomUUID();
        UUID machineId = UUID.randomUUID();
        UUID souleaterId = UUID.randomUUID();
        Permanent machinePerm = mock(Permanent.class);
        Permanent souleaterPerm = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, machineId)).thenReturn(machinePerm);
        when(gameQueryService.findPermanentById(gameData, souleaterId)).thenReturn(souleaterPerm);
        when(gameQueryService.getEffectivePower(gameData, machinePerm)).thenReturn(4);
        when(gameQueryService.getEffectivePower(gameData, souleaterPerm)).thenReturn(1);
        when(gameQueryService.hasKeyword(eq(gameData), eq(machinePerm), eq(Keyword.INDESTRUCTIBLE)))
                .thenReturn(false);
        when(gameQueryService.hasKeyword(eq(gameData), eq(souleaterPerm), eq(Keyword.INDESTRUCTIBLE)))
                .thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                aiPlayerId, 7, UUID.randomUUID(), "Geth, Lord of the Vault", 5,
                List.of(target(souleaterId, "Blinding Souleater", 3),
                        target(machineId, "Diabolic Machine", 4)),
                false, false, false);

        strategy.answer(interaction, context(aiPlayerId));

        ArgumentCaptor<CombatDamageAssignedRequest> captor =
                ArgumentCaptor.forClass(CombatDamageAssignedRequest.class);
        verify(gameActions).handleCombatDamageAssigned(eq(selfConnection), captor.capture());

        CombatDamageAssignedRequest request = captor.getValue();
        assertThat(request.attackerIndex()).isEqualTo(7);
        assertThat(request.damageAssignments()).containsExactlyInAnyOrderEntriesOf(Map.of(
                machineId.toString(), 4,
                souleaterId.toString(), 1));
    }

    @Test
    @DisplayName("Trample: assignment includes overflow player with string UUID key")
    void sendsTrampleOverflowToPlayer() throws Exception {
        UUID aiPlayerId = UUID.randomUUID();
        UUID blockerId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        Permanent blocker = mock(Permanent.class);

        when(gameQueryService.findPermanentById(gameData, blockerId)).thenReturn(blocker);
        when(gameQueryService.hasKeyword(eq(gameData), eq(blocker), eq(Keyword.INDESTRUCTIBLE)))
                .thenReturn(false);

        var interaction = new PendingInteraction.CombatDamageAssignment(
                aiPlayerId, 3, UUID.randomUUID(), "Trampler", 5,
                List.of(target(blockerId, "Blocker", 2),
                        new CombatDamageTarget(playerId, "Defender", 0, 0, true)),
                true, false, false);

        strategy.answer(interaction, context(aiPlayerId));

        ArgumentCaptor<CombatDamageAssignedRequest> captor =
                ArgumentCaptor.forClass(CombatDamageAssignedRequest.class);
        verify(gameActions).handleCombatDamageAssigned(eq(selfConnection), captor.capture());

        assertThat(captor.getValue().attackerIndex()).isEqualTo(3);
        assertThat(captor.getValue().damageAssignments()).containsExactlyInAnyOrderEntriesOf(Map.of(
                blockerId.toString(), 2,
                playerId.toString(), 3));
    }

    @Test
    @DisplayName("Registered in AiInteractionStrategies")
    void registeredInStrategies() {
        var interaction = new PendingInteraction.CombatDamageAssignment(
                UUID.randomUUID(), 0, UUID.randomUUID(), "Attacker", 1,
                List.of(target(UUID.randomUUID(), "Blocker", 1)), false, false, false);

        assertThat(AiInteractionStrategies.forInteraction(interaction))
                .isInstanceOf(CombatDamageAssignmentAiStrategy.class);
    }

    private AiInteractionContext context(UUID aiPlayerId) {
        return new AiInteractionContext(
                gameData, UUID.randomUUID(), aiPlayerId, gameQueryService, gameActions, selfConnection);
    }

    private static CombatDamageTarget target(UUID id, String name, int toughness) {
        return new CombatDamageTarget(id, name, toughness, 0, false);
    }
}
