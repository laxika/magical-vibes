package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.GameBroadcastService;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.AutoPassService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.service.turn.UntapStepService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnProgressionServiceTest {

    @Mock private CombatService combatService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TurnCleanupService turnCleanupService;
    @Mock private UntapStepService untapStepService;
    @Mock private StepTriggerService stepTriggerService;
    @Mock private AutoPassService autoPassService;

    @InjectMocks
    private TurnProgressionService turnProgressionService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.activePlayerId = player1Id;
        gd.status = GameStatus.RUNNING;
        gd.turnNumber = 1;
    }

    private void addCardsToHand(UUID playerId, int count) {
        List<Card> hand = gd.playerHands.get(playerId);
        for (int i = 0; i < count; i++) {
            hand.add(new Card());
        }
    }

    private PendingMayAbility newMayAbility() {
        return new PendingMayAbility(new Card(), player1Id, null, "Test may ability");
    }

    // =========================================================================
    // advanceStep
    // =========================================================================

    @Nested
    @DisplayName("advanceStep")
    class AdvanceStep {

        @Test
        @DisplayName("Advances from UPKEEP to DRAW and triggers draw step")
        void advancesFromUpkeepToDraw() {
            gd.currentStep = TurnStep.UPKEEP;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.DRAW);
            verify(stepTriggerService).handleDrawStep(gd);
        }

        @Test
        @DisplayName("Advances from DRAW to PRECOMBAT_MAIN and triggers precombat main triggers")
        void advancesFromDrawToPrecombatMain() {
            gd.currentStep = TurnStep.DRAW;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
            verify(stepTriggerService).handlePrecombatMainTriggers(gd);
        }

        @Test
        @DisplayName("Advances to UPKEEP and triggers upkeep triggers")
        void advancesToUpkeepAndTriggers() {
            gd.currentStep = TurnStep.UNTAP;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.UPKEEP);
            verify(stepTriggerService).handleUpkeepTriggers(gd);
        }

        @Test
        @DisplayName("Advances to DECLARE_ATTACKERS and handles declare attackers step")
        void advancesToDeclareAttackers() {
            gd.currentStep = TurnStep.BEGINNING_OF_COMBAT;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_ATTACKERS);
            verify(combatService).handleDeclareAttackersStep(gd);
        }

        @Test
        @DisplayName("Advances to DECLARE_BLOCKERS and handles declare blockers step")
        void advancesToDeclareBlockers() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            when(combatService.getAttackingCreatureIndices(gd, player1Id)).thenReturn(List.of(0));
            when(combatService.handleDeclareBlockersStep(gd)).thenReturn(CombatResult.DONE);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_BLOCKERS);
            verify(combatService).handleDeclareBlockersStep(gd);
        }

        @Test
        @DisplayName("Advances to COMBAT_DAMAGE and resolves combat damage")
        void advancesToCombatDamage() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;
            when(combatService.resolveCombatDamage(gd)).thenReturn(CombatResult.DONE);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.COMBAT_DAMAGE);
            verify(combatService).resolveCombatDamage(gd);
        }

        @Test
        @DisplayName("Advances to END_OF_COMBAT and clears combat state")
        void advancesToEndOfCombat() {
            gd.currentStep = TurnStep.COMBAT_DAMAGE;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            verify(combatService).clearCombatState(gd);
        }

        @Test
        @DisplayName("Advances to END_STEP and triggers end step triggers")
        void advancesToEndStep() {
            gd.currentStep = TurnStep.POSTCOMBAT_MAIN;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
            verify(stepTriggerService).handleEndStepTriggers(gd);
        }

        @Test
        @DisplayName("Clears priority passed on each advance")
        void clearsPriorityPassed() {
            gd.currentStep = TurnStep.UPKEEP;
            gd.priorityPassedBy.add(player1Id);
            gd.priorityPassedBy.add(player2Id);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Drains mana pools on each advance")
        void drainsManaPoolsOnAdvance() {
            gd.currentStep = TurnStep.UPKEEP;

            turnProgressionService.advanceStep(gd);

            verify(turnCleanupService).drainManaPools(gd);
        }

        @Test
        @DisplayName("Logs and broadcasts on each advance")
        void logsAndBroadcasts() {
            gd.currentStep = TurnStep.UPKEEP;

            turnProgressionService.advanceStep(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Step: Draw"));
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Does not proceed after game is FINISHED")
        void doesNotProceedAfterFinished() {
            gd.currentStep = TurnStep.UPKEEP;
            // Simulate game ending during broadcast
            doAnswer(inv -> { gd.status = GameStatus.FINISHED; return null; })
                    .when(gameBroadcastService).broadcastGameState(gd);

            turnProgressionService.advanceStep(gd);

            verify(stepTriggerService, never()).handleDrawStep(gd);
        }
    }

    // =========================================================================
    // advanceStep — CR 508.8 skip empty combat
    // =========================================================================

    @Nested
    @DisplayName("advanceStep — skip empty combat (CR 508.8)")
    class SkipEmptyCombat {

        @Test
        @DisplayName("Skips to END_OF_COMBAT when no creatures are attacking")
        void skipsToEndOfCombatWhenNoAttackers() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            when(combatService.getAttackingCreatureIndices(gd, player1Id)).thenReturn(List.of());

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
            verify(combatService).clearCombatState(gd);
            verify(combatService, never()).handleDeclareBlockersStep(any());
        }

        @Test
        @DisplayName("Proceeds to DECLARE_BLOCKERS when attackers are present")
        void proceedsToDeclareBlockersWhenAttackersPresent() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            when(combatService.getAttackingCreatureIndices(gd, player1Id)).thenReturn(List.of(0));
            when(combatService.handleDeclareBlockersStep(gd)).thenReturn(CombatResult.DONE);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_BLOCKERS);
        }
    }

    // =========================================================================
    // advanceStep — end-of-combat sacrifices/exiles
    // =========================================================================

    @Nested
    @DisplayName("advanceStep — end-of-combat sacrifices and exiles")
    class EndOfCombatSacrificesExiles {

        @Test
        @DisplayName("Processes sacrifices when leaving END_OF_COMBAT with pending sacrifices")
        void processesEndOfCombatSacrifices() {
            gd.currentStep = TurnStep.END_OF_COMBAT;
            gd.permanentsToSacrificeAtEndOfCombat.add(UUID.randomUUID());

            turnProgressionService.advanceStep(gd);

            verify(combatService).processEndOfCombatSacrifices(gd);
            verify(combatService).processEndOfCombatExiles(gd);
            // Should NOT advance step
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        }

        @Test
        @DisplayName("Processes exiles when leaving END_OF_COMBAT with pending token exiles")
        void processesEndOfCombatExiles() {
            gd.currentStep = TurnStep.END_OF_COMBAT;
            gd.pendingTokenExilesAtEndOfCombat.add(UUID.randomUUID());

            turnProgressionService.advanceStep(gd);

            verify(combatService).processEndOfCombatSacrifices(gd);
            verify(combatService).processEndOfCombatExiles(gd);
            assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);
        }

        @Test
        @DisplayName("Clears priority passed after processing end-of-combat sacrifices")
        void clearsPriorityAfterSacrifices() {
            gd.currentStep = TurnStep.END_OF_COMBAT;
            gd.permanentsToSacrificeAtEndOfCombat.add(UUID.randomUUID());
            gd.priorityPassedBy.add(player1Id);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Advances normally from END_OF_COMBAT when no pending sacrifices or exiles")
        void advancesNormallyWhenNoPending() {
            gd.currentStep = TurnStep.END_OF_COMBAT;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
            verify(combatService, never()).processEndOfCombatSacrifices(any());
        }
    }

    // =========================================================================
    // advanceStep — additional combat phases
    // =========================================================================

    @Nested
    @DisplayName("advanceStep — additional combat/main phase pairs")
    class AdditionalCombatPhases {

        @Test
        @DisplayName("Loops back to BEGINNING_OF_COMBAT when additionalCombatMainPhasePairs > 0")
        void loopsBackToCombatWhenExtraPairsRemain() {
            gd.currentStep = TurnStep.POSTCOMBAT_MAIN;
            gd.additionalCombatMainPhasePairs = 1;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
        }

        @Test
        @DisplayName("Decrements additionalCombatMainPhasePairs each time")
        void decrementsExtraPairs() {
            gd.currentStep = TurnStep.POSTCOMBAT_MAIN;
            gd.additionalCombatMainPhasePairs = 3;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(2);
        }

        @Test
        @DisplayName("Advances to END_STEP normally when additionalCombatMainPhasePairs is 0")
        void advancesToEndStepWhenNoExtraPairs() {
            gd.currentStep = TurnStep.POSTCOMBAT_MAIN;
            gd.additionalCombatMainPhasePairs = 0;

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        }
    }

    // =========================================================================
    // advanceStep — CLEANUP step
    // =========================================================================

    @Nested
    @DisplayName("advanceStep — cleanup step")
    class CleanupStep {

        @Test
        @DisplayName("Triggers discard when hand exceeds max hand size")
        void triggersDiscardWhenHandExceedsMax() {
            gd.currentStep = TurnStep.END_STEP;
            // Put 9 cards in active player's hand (max is 7)
            addCardsToHand(player1Id, 9);
            when(turnCleanupService.getMaxHandSize(gd, player1Id)).thenReturn(7);
            when(turnCleanupService.hasNoMaximumHandSize(gd, player1Id)).thenReturn(false);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
            assertThat(gd.cleanupDiscardPending).isTrue();
            assertThat(gd.discardCausedByOpponent).isFalse();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }

        @Test
        @DisplayName("Does not trigger discard when hand is within max hand size")
        void noDiscardWhenHandWithinMax() {
            gd.currentStep = TurnStep.END_STEP;
            // Put 5 cards in hand (below max of 7)
            addCardsToHand(player1Id, 5);
            when(turnCleanupService.getMaxHandSize(gd, player1Id)).thenReturn(7);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.cleanupDiscardPending).isFalse();
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
            verify(turnCleanupService).applyCleanupResets(gd);
        }

        @Test
        @DisplayName("Does not trigger discard when player has no maximum hand size")
        void noDiscardWhenNoMaxHandSize() {
            gd.currentStep = TurnStep.END_STEP;
            addCardsToHand(player1Id, 20);
            when(turnCleanupService.getMaxHandSize(gd, player1Id)).thenReturn(7);
            when(turnCleanupService.hasNoMaximumHandSize(gd, player1Id)).thenReturn(true);

            turnProgressionService.advanceStep(gd);

            assertThat(gd.cleanupDiscardPending).isFalse();
            verify(playerInputService, never()).beginDiscardChoice(any(), any());
            verify(turnCleanupService).applyCleanupResets(gd);
        }

        @Test
        @DisplayName("Applies cleanup resets when no discard needed (CR 514.2)")
        void appliesCleanupResetsWhenNoDiscard() {
            gd.currentStep = TurnStep.END_STEP;
            // Empty hand — no discard needed
            when(turnCleanupService.getMaxHandSize(gd, player1Id)).thenReturn(7);

            turnProgressionService.advanceStep(gd);

            verify(turnCleanupService).applyCleanupResets(gd);
        }

        @Test
        @DisplayName("Max hand size is clamped to at least 0")
        void maxHandSizeClampedToZero() {
            gd.currentStep = TurnStep.END_STEP;
            // 1 card in hand, max hand size returned as -2 (should be clamped to 0)
            addCardsToHand(player1Id, 1);
            when(turnCleanupService.getMaxHandSize(gd, player1Id)).thenReturn(-2);
            when(turnCleanupService.hasNoMaximumHandSize(gd, player1Id)).thenReturn(false);

            turnProgressionService.advanceStep(gd);

            // With 1 card and max 0, discard count = 1
            assertThat(gd.cleanupDiscardPending).isTrue();
            verify(playerInputService).beginDiscardChoice(gd, player1Id);
        }
    }

    // =========================================================================
    // advanceTurn
    // =========================================================================

    @Nested
    @DisplayName("advanceTurn")
    class AdvanceTurn {

        @Test
        @DisplayName("Switches active player to the other player")
        void switchesActivePlayer() {
            gd.activePlayerId = player1Id;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.activePlayerId).isEqualTo(player2Id);
        }

        @Test
        @DisplayName("Uses extra turn queue when available")
        void usesExtraTurnQueue() {
            gd.activePlayerId = player1Id;
            gd.extraTurns.addLast(player1Id);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.activePlayerId).isEqualTo(player1Id);
            assertThat(gd.extraTurns).isEmpty();
        }

        @Test
        @DisplayName("Increments turn number")
        void incrementsTurnNumber() {
            gd.turnNumber = 5;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.turnNumber).isEqualTo(6);
        }

        @Test
        @DisplayName("Resets current step to UNTAP")
        void resetsCurrentStep() {
            gd.currentStep = TurnStep.CLEANUP;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);
        }

        @Test
        @DisplayName("Clears awaiting input")
        void clearsAwaitingInput() {
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("Clears priority passed set")
        void clearsPriorityPassed() {
            gd.priorityPassedBy.add(player1Id);
            gd.priorityPassedBy.add(player2Id);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Clears per-turn tracking maps")
        void clearsPerTurnTracking() {
            gd.landsPlayedThisTurn.put(player1Id, 1);
            gd.recordSpellCast(player1Id, new GrizzlyBears());
            gd.recordSpellCast(player1Id, new GrizzlyBears());
            gd.recordSpellCast(player1Id, new GrizzlyBears());
            gd.playersDeclaredAttackersThisTurn.add(player1Id);
            gd.playersSilencedThisTurn.add(player1Id);
            gd.activatedAbilityUsesThisTurn.put(player1Id, new HashMap<>());
            gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn.put(player1Id, new HashSet<>());
            gd.creatureDeathCountThisTurn.put(player1Id, 2);
            gd.cardsDrawnThisTurn.put(player1Id, 3);
            gd.combatDamageToPlayersThisTurn.put(UUID.randomUUID(), new HashSet<>());
            gd.playersDealtDamageThisTurn.add(player1Id);
            gd.creatureCardsDamagedThisTurnBySourcePermanent.put(UUID.randomUUID(), new HashSet<>());
            gd.creatureGivingControllerPoisonOnDeathThisTurn.put(UUID.randomUUID(), 1);
            gd.paidSearchTaxPermanentIds.put(player1Id, new HashSet<>());

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.landsPlayedThisTurn).isEmpty();
            assertThat(gd.isSpellsCastThisTurnEmpty()).isTrue();
            assertThat(gd.playersDeclaredAttackersThisTurn).isEmpty();
            assertThat(gd.playersSilencedThisTurn).isEmpty();
            assertThat(gd.activatedAbilityUsesThisTurn).isEmpty();
            assertThat(gd.creatureCardsPutIntoGraveyardFromBattlefieldThisTurn).isEmpty();
            assertThat(gd.creatureDeathCountThisTurn).isEmpty();
            assertThat(gd.cardsDrawnThisTurn).isEmpty();
            assertThat(gd.combatDamageToPlayersThisTurn).isEmpty();
            assertThat(gd.playersDealtDamageThisTurn).isEmpty();
            assertThat(gd.creatureCardsDamagedThisTurnBySourcePermanent).isEmpty();
            assertThat(gd.creatureGivingControllerPoisonOnDeathThisTurn).isEmpty();
            assertThat(gd.paidSearchTaxPermanentIds).isEmpty();
        }

        @Test
        @DisplayName("Snapshots spellsCastThisTurn into spellsCastLastTurn before clearing")
        void snapshotsSpellsCastLastTurn() {
            gd.recordSpellCast(player1Id, new GrizzlyBears());
            gd.recordSpellCast(player1Id, new GrizzlyBears());
            gd.recordSpellCast(player2Id, new GrizzlyBears());
            gd.spellsCastLastTurn.put(player1Id, 99); // old data should be replaced

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.spellsCastLastTurn).containsEntry(player1Id, 2);
            assertThat(gd.spellsCastLastTurn).containsEntry(player2Id, 1);
            assertThat(gd.isSpellsCastThisTurnEmpty()).isTrue();
        }

        @Test
        @DisplayName("Resets additionalCombatMainPhasePairs to 0")
        void resetsAdditionalCombatPairs() {
            gd.additionalCombatMainPhasePairs = 2;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(0);
        }

        @Test
        @DisplayName("Resets cleanupDiscardPending to false")
        void resetsCleanupDiscardPending() {
            gd.cleanupDiscardPending = true;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.cleanupDiscardPending).isFalse();
        }

        @Test
        @DisplayName("Drains mana pools at turn boundary")
        void drainsManaPoolsAtTurnBoundary() {
            turnProgressionService.advanceTurn(gd);

            verify(turnCleanupService).drainManaPools(gd);
        }

        @Test
        @DisplayName("Untaps permanents for the new active player")
        void untapsPermanents() {
            gd.activePlayerId = player1Id;

            turnProgressionService.advanceTurn(gd);

            // New active player is player2
            verify(untapStepService).untapPermanents(gd, player2Id);
        }

        @Test
        @DisplayName("Clears mind control state from the ending turn")
        void clearsMindControlState() {
            gd.mindControlledPlayerId = player1Id;
            gd.mindControllerPlayerId = player2Id;

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.mindControlledPlayerId).isNull();
            assertThat(gd.mindControllerPlayerId).isNull();
        }

        @Test
        @DisplayName("Sets permanentsEnteredBattlefieldThisTurn to empty")
        void clearsPermanentsEnteredThisTurn() {
            gd.permanentsEnteredBattlefieldThisTurn.put(player1Id, new ArrayList<>());

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.permanentsEnteredBattlefieldThisTurn).isEmpty();
        }

        @Test
        @DisplayName("Processes pending may abilities before completing turn advance")
        void processesPermayAbilitiesIfPresent() {
            gd.pendingMayAbilities.add(newMayAbility());

            turnProgressionService.advanceTurn(gd);

            verify(playerInputService).processNextMayAbility(gd);
            // completeTurnAdvance should NOT be called — awaiting may ability
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), org.mockito.ArgumentMatchers.contains("Turn"));
        }
    }

    // =========================================================================
    // advanceTurn — Mindslaver turn control
    // =========================================================================

    @Nested
    @DisplayName("advanceTurn — Mindslaver turn control")
    class MindslaverTurnControl {

        @Test
        @DisplayName("Activates Mindslaver control when pending turn control exists")
        void activatesMindslaver() {
            gd.activePlayerId = player1Id;
            gd.pendingTurnControl.put(player2Id, player1Id);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.mindControlledPlayerId).isEqualTo(player2Id);
            assertThat(gd.mindControllerPlayerId).isEqualTo(player1Id);
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Player1 controls Player2 this turn (Mindslaver)."));
        }

        @Test
        @DisplayName("Does not activate Mindslaver when controller is no longer in the game")
        void doesNotActivateWhenControllerNotInGame() {
            gd.activePlayerId = player1Id;
            UUID unknownPlayer = UUID.randomUUID();
            gd.pendingTurnControl.put(player2Id, unknownPlayer);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.mindControlledPlayerId).isNull();
            assertThat(gd.mindControllerPlayerId).isNull();
        }

        @Test
        @DisplayName("Consumes the pending turn control entry")
        void consumesPendingTurnControl() {
            gd.activePlayerId = player1Id;
            gd.pendingTurnControl.put(player2Id, player1Id);

            turnProgressionService.advanceTurn(gd);

            assertThat(gd.pendingTurnControl).isEmpty();
        }
    }

    // =========================================================================
    // completeTurnAdvance
    // =========================================================================

    @Nested
    @DisplayName("completeTurnAdvance")
    class CompleteTurnAdvance {

        @Test
        @DisplayName("Logs turn start message")
        void logsTurnStart() {
            gd.activePlayerId = player1Id;
            gd.turnNumber = 3;

            turnProgressionService.completeTurnAdvance(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Turn 3 begins. Player1's turn."));
        }

        @Test
        @DisplayName("Broadcasts game state after turn advance")
        void broadcastsGameState() {
            gd.activePlayerId = player1Id;
            gd.turnNumber = 1;

            turnProgressionService.completeTurnAdvance(gd);

            verify(gameBroadcastService).broadcastGameState(gd);
        }
    }

    // =========================================================================
    // handleCombatResult
    // =========================================================================

    @Nested
    @DisplayName("handleCombatResult")
    class HandleCombatResult {

        @Test
        @DisplayName("DONE does not advance or auto-pass")
        void doneDoesNothing() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;

            turnProgressionService.handleCombatResult(CombatResult.DONE, gd);

            // Step should not change
            assertThat(gd.currentStep).isEqualTo(TurnStep.DECLARE_BLOCKERS);
            verify(autoPassService, never()).resolveAutoPass(any(), any());
            verify(autoPassService, never()).resolveAutoPassCombatTriggers(any());
        }

        @Test
        @DisplayName("AUTO_PASS_ONLY resolves auto pass but does not advance step")
        void autoPassOnlyResolvesAutoPass() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;

            turnProgressionService.handleCombatResult(CombatResult.AUTO_PASS_ONLY, gd);

            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }

        @Test
        @DisplayName("AUTO_PASS_RESOLVE_COMBAT_TRIGGERS resolves combat triggers and auto pass")
        void autoPassResolveCombatTriggers() {
            gd.currentStep = TurnStep.COMBAT_DAMAGE;

            turnProgressionService.handleCombatResult(CombatResult.AUTO_PASS_RESOLVE_COMBAT_TRIGGERS, gd);

            verify(autoPassService).resolveAutoPassCombatTriggers(gd);
            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }

        @Test
        @DisplayName("ADVANCE_ONLY advances step but does not auto-pass")
        void advanceOnlyAdvancesStep() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;

            turnProgressionService.handleCombatResult(CombatResult.ADVANCE_ONLY, gd);

            // Step should have advanced (from DECLARE_BLOCKERS)
            assertThat(gd.currentStep).isNotEqualTo(TurnStep.DECLARE_BLOCKERS);
            verify(autoPassService, never()).resolveAutoPass(any(), any());
        }

        @Test
        @DisplayName("ADVANCE_AND_AUTO_PASS both advances step and resolves auto pass")
        void advanceAndAutoPass() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;

            turnProgressionService.handleCombatResult(CombatResult.ADVANCE_AND_AUTO_PASS, gd);

            // Step should have advanced
            assertThat(gd.currentStep).isNotEqualTo(TurnStep.DECLARE_BLOCKERS);
            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }
    }

    // =========================================================================
    // resolveAutoPass
    // =========================================================================

    @Nested
    @DisplayName("resolveAutoPass")
    class ResolveAutoPass {

        @Test
        @DisplayName("Processes pending may abilities before auto-passing when stack is empty")
        void processesMayAbilitiesBeforeAutoPass() {
            gd.pendingMayAbilities.add(newMayAbility());

            turnProgressionService.resolveAutoPass(gd);

            verify(playerInputService).processNextMayAbility(gd);
            verify(autoPassService, never()).resolveAutoPass(any(), any());
        }

        @Test
        @DisplayName("Does not process may abilities when stack is non-empty")
        void doesNotProcessMayAbilitiesWithStack() {
            gd.pendingMayAbilities.add(newMayAbility());
            gd.stack.add(new StackEntry(
                    StackEntryType.INSTANT_SPELL,
                    new Card(), player1Id, "Test", List.of(), 0));

            turnProgressionService.resolveAutoPass(gd);

            verify(playerInputService, never()).processNextMayAbility(any());
            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }

        @Test
        @DisplayName("Does not process may abilities when awaiting input")
        void doesNotProcessMayAbilitiesWhenAwaitingInput() {
            gd.pendingMayAbilities.add(newMayAbility());
            gd.interaction.setAwaitingInput(AwaitingInput.PERMANENT_CHOICE);

            turnProgressionService.resolveAutoPass(gd);

            verify(playerInputService, never()).processNextMayAbility(any());
            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }

        @Test
        @DisplayName("Delegates to autoPassService when no may abilities pending")
        void delegatesToAutoPassService() {
            turnProgressionService.resolveAutoPass(gd);

            verify(autoPassService).resolveAutoPass(eq(gd), any());
        }
    }

    // =========================================================================
    // Delegation methods
    // =========================================================================

    @Nested
    @DisplayName("Delegation methods")
    class DelegationMethods {

        @Test
        @DisplayName("applyCleanupResets delegates to turnCleanupService")
        void applyCleanupResetsDelegates() {
            turnProgressionService.applyCleanupResets(gd);

            verify(turnCleanupService).applyCleanupResets(gd);
        }

        @Test
        @DisplayName("processNextUpkeepPlayerTarget delegates to stepTriggerService")
        void processNextUpkeepPlayerTargetDelegates() {
            turnProgressionService.processNextUpkeepPlayerTarget(gd);

            verify(stepTriggerService).processNextUpkeepPlayerTarget(gd);
        }

        @Test
        @DisplayName("processNextUpkeepCopyTarget delegates to stepTriggerService")
        void processNextUpkeepCopyTargetDelegates() {
            turnProgressionService.processNextUpkeepCopyTarget(gd);

            verify(stepTriggerService).processNextUpkeepCopyTarget(gd);
        }

        @Test
        @DisplayName("processNextCapriciousEfreetTarget delegates to stepTriggerService")
        void processNextCapriciousEfreetTargetDelegates() {
            turnProgressionService.processNextCapriciousEfreetTarget(gd);

            verify(stepTriggerService).processNextCapriciousEfreetTarget(gd);
        }

        @Test
        @DisplayName("processNextEndStepTriggerTarget delegates to stepTriggerService")
        void processNextEndStepTriggerTargetDelegates() {
            turnProgressionService.processNextEndStepTriggerTarget(gd);

            verify(stepTriggerService).processNextEndStepTriggerTarget(gd);
        }
    }

    // =========================================================================
    // advanceTurn — turn boundary from CLEANUP
    // =========================================================================

    @Nested
    @DisplayName("advanceStep — advances turn from CLEANUP")
    class AdvanceTurnFromCleanup {

        @Test
        @DisplayName("Advances to a new turn when stepping past CLEANUP (next is null)")
        void advancesToNewTurnFromCleanup() {
            gd.currentStep = TurnStep.CLEANUP;
            gd.activePlayerId = player1Id;

            turnProgressionService.advanceStep(gd);

            // advanceTurn should have been called, switching active player
            assertThat(gd.activePlayerId).isEqualTo(player2Id);
            assertThat(gd.currentStep).isEqualTo(TurnStep.UNTAP);
            assertThat(gd.turnNumber).isEqualTo(2);
        }
    }
}
