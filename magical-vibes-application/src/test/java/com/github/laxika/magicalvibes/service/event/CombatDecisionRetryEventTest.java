package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.CombatDamageTarget;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventBatch;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.MulliganService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.ability.AbilityActivationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.interaction.AttackerDeclarationInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.BlockerDeclarationInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.CombatDamageAssignmentInteractionHandler;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.state.StateBasedActionService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CombatDecisionRetryEventTest {

    private UUID activePlayerId;
    private UUID defendingPlayerId;
    private GameData gameData;
    private CombatService combatService;
    private GameService gameService;
    private List<GameEventBatch> batches;

    @BeforeEach
    void setUp() {
        activePlayerId = UUID.randomUUID();
        defendingPlayerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "combat-retry", activePlayerId, "Attacker");
        gameData.activePlayerId = activePlayerId;
        gameData.playerIds.addAll(List.of(activePlayerId, defendingPlayerId));
        gameData.orderedPlayerIds.addAll(List.of(activePlayerId, defendingPlayerId));
        gameData.playerIdToName.put(activePlayerId, "Attacker");
        gameData.playerIdToName.put(defendingPlayerId, "Defender");

        batches = new ArrayList<>();
        GameMutationCoordinator coordinator = new GameMutationCoordinator(
                new GameEventDispatcher(List.of(batches::add)));
        InteractionHandlerRegistry interactions = new InteractionHandlerRegistry(() -> coordinator);
        combatService = mock(CombatService.class);
        TurnProgressionService turnProgression = mock(TurnProgressionService.class);
        interactions.register(new AttackerDeclarationInteractionHandler(
                combatService,
                mock(StateBasedActionService.class),
                turnProgression));
        interactions.register(new BlockerDeclarationInteractionHandler(
                combatService,
                turnProgression));
        interactions.register(new CombatDamageAssignmentInteractionHandler(
                combatService,
                turnProgression));

        gameService = new GameService(
                mock(GameQueryService.class),
                mock(GameLogService.class),
                combatService,
                turnProgression,
                interactions,
                mock(SpellCastingService.class),
                mock(StackResolutionService.class),
                mock(AbilityActivationService.class),
                mock(MulliganService.class),
                mock(GameOutcomeService.class),
                coordinator);
    }

    @Test
    void rejectedAttackerDeclarationReopensTheSameDecisionForTheMindslaverController() {
        UUID controllerId = defendingPlayerId;
        gameData.mindControlledPlayerId = activePlayerId;
        gameData.mindControllerPlayerId = controllerId;
        gameData.interaction.beginInteraction(
                new PendingInteraction.AttackerDeclaration(activePlayerId));
        UUID decisionId = gameData.interaction.activeDecisionId();
        when(combatService.declareAttackers(
                eq(gameData), any(Player.class), eq(List.of(9)), isNull(), isNull()))
                .thenThrow(new IllegalStateException("Invalid attacker index: 9"));

        assertThatThrownBy(() -> gameService.declareAttackers(
                gameData, new Player(controllerId, "Controller"), List.of(9)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid attacker index: 9");

        assertRetry(decisionId, activePlayerId,
                GameEventFact.DecisionKind.ATTACKER_DECLARATION, controllerId);
    }

    @Test
    void rejectedBlockerDeclarationReopensTheSameDecisionForTheDefender() {
        gameData.interaction.beginInteraction(
                new PendingInteraction.BlockerDeclaration(defendingPlayerId));
        UUID decisionId = gameData.interaction.activeDecisionId();
        List<BlockerAssignment> invalid = List.of(new BlockerAssignment(7, 0));
        when(combatService.declareBlockers(eq(gameData), any(Player.class), eq(invalid)))
                .thenThrow(new IllegalStateException("Invalid blocker index: 7"));

        assertThatThrownBy(() -> gameService.declareBlockers(
                gameData, new Player(defendingPlayerId, "Defender"), invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid blocker index: 7");

        assertRetry(decisionId, defendingPlayerId,
                GameEventFact.DecisionKind.BLOCKER_DECLARATION, defendingPlayerId);
    }

    @Test
    void rejectedDamageAssignmentReopensTheSameDecisionWithItsFinalizedTargets() {
        UUID attackerPermanentId = UUID.randomUUID();
        UUID blockerId = UUID.randomUUID();
        PendingInteraction.CombatDamageAssignment interaction =
                new PendingInteraction.CombatDamageAssignment(
                        activePlayerId,
                        2,
                        attackerPermanentId,
                        "Attacker",
                        6,
                        List.of(new CombatDamageTarget(
                                blockerId, "Blocker", 5, 0, false)),
                        true,
                        true,
                        false);
        gameData.interaction.beginInteraction(interaction);
        UUID decisionId = gameData.interaction.activeDecisionId();
        Map<UUID, Integer> invalid = Map.of(blockerId, 1);
        doThrow(new IllegalStateException("Total assigned damage must equal 6"))
                .when(combatService)
                .handleCombatDamageAssigned(
                        eq(gameData), any(Player.class), eq(2), eq(invalid));

        assertThatThrownBy(() -> gameService.handleCombatDamageAssigned(
                gameData, new Player(activePlayerId, "Attacker"), 2, invalid))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Total assigned damage must equal 6");

        assertThat(gameData.interaction.activeInteraction()).isSameAs(interaction);
        assertRetry(decisionId, activePlayerId,
                GameEventFact.DecisionKind.COMBAT_DAMAGE_ASSIGNMENT, activePlayerId);
    }

    private void assertRetry(
            UUID decisionId,
            UUID decidingPlayerId,
            GameEventFact.DecisionKind decisionKind,
            UUID recipientId) {
        assertThat(batches).singleElement().satisfies(batch -> {
            assertThat(batch.events()).singleElement().satisfies(envelope -> {
                assertThat(envelope.audience())
                        .isEqualTo(GameEventAudience.player(recipientId));
                assertThat(envelope.fact()).isEqualTo(
                        new GameEventFact.DecisionRequested(
                                decisionId, decidingPlayerId, decisionKind));
            });
        });
        assertThat(gameData.interaction.activeDecisionId()).isEqualTo(decisionId);
    }
}
