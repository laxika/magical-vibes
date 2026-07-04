package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignmentNotification;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDamageAssignmentInteractionHandlerTest {

    @Mock private SessionManager sessionManager;
    @Mock private CombatService combatService;
    @Mock private TurnProgressionService turnProgressionService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new CombatDamageAssignmentInteractionHandler(
                sessionManager, combatService, turnProgressionService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
    }

    private PendingInteraction.CombatDamageAssignment assignment(UUID attackerId, UUID blockerId) {
        return new PendingInteraction.CombatDamageAssignment(
                PLAYER1_ID, 2, attackerId, "Craw Wurm", 6,
                List.of(new CombatDamageTarget(blockerId, "Grizzly Bears", 2, 0, false),
                        new CombatDamageTarget(PLAYER2_ID, "Player2", 0, 0, true)),
                true, false);
    }

    @Test
    @DisplayName("begin sets COMBAT_DAMAGE_ASSIGNMENT and sends the begin-time notification content")
    void beginSendsNotification() {
        UUID attackerId = UUID.randomUUID();
        UUID blockerId = UUID.randomUUID();

        registry.begin(gd, assignment(attackerId, blockerId));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
        CombatDamageAssignmentNotification msg = (CombatDamageAssignmentNotification) messageCaptor.getValue();
        assertThat(msg.attackerIndex()).isEqualTo(2);
        assertThat(msg.attackerPermanentId()).isEqualTo(attackerId.toString());
        assertThat(msg.attackerName()).isEqualTo("Craw Wurm");
        assertThat(msg.totalDamage()).isEqualTo(6);
        assertThat(msg.isTrample()).isTrue();
        assertThat(msg.isDeathtouch()).isFalse();
        assertThat(msg.validTargets()).hasSize(2);
        assertThat(msg.validTargets().get(0).id()).isEqualTo(blockerId.toString());
        assertThat(msg.validTargets().get(0).name()).isEqualTo("Grizzly Bears");
        assertThat(msg.validTargets().get(0).toughness()).isEqualTo(2);
        assertThat(msg.validTargets().get(0).isPlayer()).isFalse();
        assertThat(msg.validTargets().get(1).id()).isEqualTo(PLAYER2_ID.toString());
        assertThat(msg.validTargets().get(1).isPlayer()).isTrue();
    }

    @Test
    @DisplayName("dispatchAnswer applies the assignment and continues the damage-resolution loop")
    void dispatchAppliesAndContinues() {
        UUID blockerId = UUID.randomUUID();
        registry.begin(gd, assignment(UUID.randomUUID(), blockerId));
        Player player = new Player(PLAYER1_ID, "Player1");
        Map<UUID, Integer> assignments = Map.of(blockerId, 2, PLAYER2_ID, 4);
        when(combatService.resolveCombatDamage(gd)).thenReturn(CombatResult.DONE);

        boolean handled = registry.dispatchAnswer(gd, player,
                new InteractionAnswer.CombatDamageAssigned(2, assignments));

        assertThat(handled).isTrue();
        verify(combatService).handleCombatDamageAssigned(gd, player, 2, assignments);
        verify(turnProgressionService).handleCombatResult(CombatResult.DONE, gd);
    }

    @Test
    @DisplayName("an invalid assignment re-sends the prompt via resolveCombatDamage and rethrows")
    void invalidAssignmentResendsAndRethrows() {
        UUID blockerId = UUID.randomUUID();
        registry.begin(gd, assignment(UUID.randomUUID(), blockerId));
        Player player = new Player(PLAYER1_ID, "Player1");
        Map<UUID, Integer> assignments = Map.of(blockerId, 1);
        doThrow(new IllegalStateException("Total assigned damage (1) must equal attacker's combat damage (6)"))
                .when(combatService).handleCombatDamageAssigned(gd, player, 2, assignments);

        assertThatThrownBy(() -> registry.dispatchAnswer(gd, player,
                new InteractionAnswer.CombatDamageAssigned(2, assignments)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must equal attacker's combat damage");

        verify(combatService).resolveCombatDamage(gd);
        verify(turnProgressionService, never()).handleCombatResult(any(), any());
    }

    @Test
    @DisplayName("replayPrompt re-sends only to the decider")
    void replayOnlyToDecider() {
        registry.begin(gd, assignment(UUID.randomUUID(), UUID.randomUUID()));
        org.mockito.Mockito.clearInvocations(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
        verifyNoInteractions(sessionManager);

        assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
        verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(CombatDamageAssignmentNotification.class));
    }
}
