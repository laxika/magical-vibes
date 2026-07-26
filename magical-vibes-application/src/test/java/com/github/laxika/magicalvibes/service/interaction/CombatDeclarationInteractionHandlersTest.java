package com.github.laxika.magicalvibes.service.interaction;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatDeclarationInteractionHandlersTest {

    @Mock private CombatService combatService;
    @Mock private StateBasedActionService stateBasedActionService;
    @Mock private TurnProgressionService turnProgressionService;

    private InteractionHandlerRegistry registry;
    private GameData gd;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        registry = new InteractionHandlerRegistry();
        registry.register(new AttackerDeclarationInteractionHandler(
                combatService, stateBasedActionService, turnProgressionService));
        registry.register(new BlockerDeclarationInteractionHandler(
                combatService, turnProgressionService));

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
        @DisplayName("dispatchAnswer rethrows an invalid declaration without advancing combat")
        void dispatchRethrowsInvalidDeclaration() {
            registry.begin(gd, new PendingInteraction.AttackerDeclaration(PLAYER1_ID));
            Player player = new Player(PLAYER1_ID, "Player1");
            when(combatService.declareAttackers(gd, player, List.of(5), null, null))
                    .thenThrow(new IllegalStateException("Invalid attacker index: 5"));

            assertThatThrownBy(() -> registry.dispatchAnswer(gd, player,
                    new InteractionAnswer.AttackersDeclared(List.of(5), null)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Invalid attacker index: 5");
            verifyNoInteractions(stateBasedActionService, turnProgressionService);
        }
    }

    @Nested
    @DisplayName("Blocker declaration")
    class BlockerDeclaration {

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
