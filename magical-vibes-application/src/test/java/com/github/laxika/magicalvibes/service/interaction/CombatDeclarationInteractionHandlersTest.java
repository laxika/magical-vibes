package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableAttackersMessage;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDeclarationInteractionHandlersTest {

    @Mock private SessionManager sessionManager;
    @Mock private CombatService combatService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private TurnProgressionService turnProgressionService;

    @Captor private ArgumentCaptor<Object> messageCaptor;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new AttackerDeclarationInteractionHandler(
                sessionManager, combatService, gameBroadcastService, castingCostService,
                stateBasedActionService, turnProgressionService));
        registry.register(new BlockerDeclarationInteractionHandler(
                sessionManager, combatService, turnProgressionService));

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Player1");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Player1");
        gd.playerIdToName.put(PLAYER2_ID, "Player2");
        gd.activePlayerId = PLAYER1_ID;
        gd.status = GameStatus.RUNNING;
    }

    @Nested
    @DisplayName("Attacker declaration")
    class AttackerDeclaration {

        @Test
        @DisplayName("begin sets state and sends the available attackers derived from live combat state")
        void beginSendsPrompt() {
            when(combatService.getAttackableCreatureIndices(gd, PLAYER1_ID)).thenReturn(List.of(0, 2));
            when(combatService.getMustAttackIndices(gd, PLAYER1_ID, List.of(0, 2))).thenReturn(List.of(2));
            when(combatService.buildAvailableTargets(gd, PLAYER1_ID)).thenReturn(List.of());
            when(castingCostService.getAttackPaymentPerCreature(gd, PLAYER1_ID)).thenReturn(1);
            when(combatService.isOpponentForcedToAttack(gd, PLAYER1_ID)).thenReturn(true);

            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.AttackerDeclaration.class);
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), messageCaptor.capture());
            AvailableAttackersMessage msg = (AvailableAttackersMessage) messageCaptor.getValue();
            assertThat(msg.attackerIndices()).containsExactly(0, 2);
            assertThat(msg.mustAttackIndices()).containsExactly(2);
            assertThat(msg.taxPerCreature()).isEqualTo(1);
            assertThat(msg.mustAttackWithAtLeastOne()).isTrue();
        }

        @Test
        @DisplayName("dispatchAnswer runs the combat flow and feeds the result to turn progression")
        void dispatchDelegates() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            Player player = new Player(PLAYER1_ID, "Player1");
            when(combatService.declareAttackers(gd, player, List.of(0), null, null)).thenReturn(CombatResult.DONE);

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.AttackersDeclared(List.of(0), null));

            assertThat(handled).isTrue();
            verify(stateBasedActionService).performStateBasedActions(gd);
            verify(turnProgressionService).handleCombatResult(CombatResult.DONE, gd);
        }

        @Test
        @DisplayName("dispatchAnswer does not advance combat when attack-cost payment ends the game")
        void dispatchStopsWhenStateBasedActionsEndGame() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            Player player = new Player(PLAYER1_ID, "Player1");
            when(combatService.declareAttackers(gd, player, List.of(0), null, null)).thenReturn(CombatResult.DONE);
            org.mockito.Mockito.doAnswer(invocation -> {
                gd.status = GameStatus.FINISHED;
                return null;
            }).when(stateBasedActionService).performStateBasedActions(gd);

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.AttackersDeclared(List.of(0), null));

            assertThat(handled).isTrue();
            verifyNoInteractions(turnProgressionService);
        }

        @Test
        @DisplayName("dispatchAnswer re-sends the available attackers and rethrows on an invalid declaration")
        void dispatchResendsOnInvalidDeclaration() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            Player player = new Player(PLAYER1_ID, "Player1");
            when(combatService.declareAttackers(gd, player, List.of(5), null, null))
                    .thenThrow(new IllegalStateException("Invalid attacker index: 5"));

            assertThatThrownBy(() -> registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.AttackersDeclared(List.of(5), null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid attacker index: 5");
            verify(combatService).handleDeclareAttackersStep(gd);
        }

        @Test
        @DisplayName("replayPrompt re-sends only to the active player")
        void replayOnlyToDecider() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            org.mockito.Mockito.clearInvocations(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
            verifyNoInteractions(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
            verify(sessionManager).sendToPlayer(eq(PLAYER1_ID), any(AvailableAttackersMessage.class));
        }
    }

    @Nested
    @DisplayName("Blocker declaration")
    class BlockerDeclaration {

        @Test
        @DisplayName("begin sets state and sends the available blockers derived from live combat state")
        void beginSendsPrompt() {
            AvailableBlockersMessage message = mock(AvailableBlockersMessage.class);
            when(combatService.getBlockableCreatureIndices(gd, PLAYER2_ID)).thenReturn(List.of(1));
            when(combatService.getBlockableAttackerIndices(gd, PLAYER1_ID, PLAYER2_ID)).thenReturn(List.of(0));
            when(combatService.buildAvailableBlockersMessage(gd, List.of(1), List.of(0), PLAYER2_ID, PLAYER1_ID))
                    .thenReturn(message);

            registry.begin(gd, new PendingInteraction.BlockerDeclaration(PLAYER2_ID));

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.BlockerDeclaration.class);
            verify(sessionManager).sendToPlayer(PLAYER2_ID, message);
        }

        @Test
        @DisplayName("dispatchAnswer runs the combat flow and feeds the result to turn progression")
        void dispatchDelegates() {
            registry.begin(gd, new PendingInteraction.BlockerDeclaration(PLAYER2_ID));
            Player player = new Player(PLAYER2_ID, "Player2");
            List<BlockerAssignment> assignments = List.of(new BlockerAssignment(1, 0));
            when(combatService.declareBlockers(gd, player, assignments)).thenReturn(CombatResult.DONE);

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.BlockersDeclared(assignments));

            assertThat(handled).isTrue();
            verify(turnProgressionService).handleCombatResult(CombatResult.DONE, gd);
        }

        @Test
        @DisplayName("replayPrompt re-sends only to the defender")
        void replayOnlyToDecider() {
            AvailableBlockersMessage message = mock(AvailableBlockersMessage.class);
            when(combatService.buildAvailableBlockersMessage(eq(gd), any(), any(), eq(PLAYER2_ID), eq(PLAYER1_ID)))
                    .thenReturn(message);
            registry.begin(gd, new PendingInteraction.BlockerDeclaration(PLAYER2_ID));
            org.mockito.Mockito.clearInvocations(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER1_ID)).isTrue();
            verifyNoInteractions(sessionManager);

            assertThat(registry.replayPrompt(gd, PLAYER2_ID)).isTrue();
            verify(sessionManager).sendToPlayer(PLAYER2_ID, message);
        }
    }

    @Nested
    @DisplayName("Answer-shape gating")
    class AnswerShapeGating {

        @Test
        @DisplayName("a blocker answer does not dispatch into an attacker declaration")
        void wrongShapeMisses() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            Player player = new Player(PLAYER1_ID, "Player1");

            boolean handled = registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.BlockersDeclared(List.of()));

            assertThat(handled).isFalse();
            verifyNoInteractions(turnProgressionService);
        }
    }
}
