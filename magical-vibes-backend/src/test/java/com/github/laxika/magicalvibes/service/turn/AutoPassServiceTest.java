package com.github.laxika.magicalvibes.service.turn;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.StackResolutionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutoPassServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private TriggerCollectionService triggerCollectionService;

    @Mock
    private StackResolutionService stackResolutionService;

    @Mock
    private StepTriggerService stepTriggerService;

    @Mock
    private CombatAttackService combatAttackService;

    @InjectMocks
    private AutoPassService sut;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
        gd.playerBattlefields.put(player1Id, new ArrayList<>());
        gd.playerBattlefields.put(player2Id, new ArrayList<>());
    }

    @Nested
    @DisplayName("resolveAutoPass")
    class ResolveAutoPass {

        @Test
        @DisplayName("Returns immediately when game is not running")
        void returnsImmediatelyWhenGameNotRunning() {
            gd.status = GameStatus.FINISHED;
            TurnStep stepBefore = gd.currentStep;

            sut.resolveAutoPass(gd, ignored -> {});

            assertThat(gd.currentStep).isEqualTo(stepBefore);
            verify(gameBroadcastService, never()).broadcastGameState(any());
        }

        @Test
        @DisplayName("Stops when stack is non-empty")
        void stopsWhenStackIsNonEmpty() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            sut.resolveAutoPass(gd, ignored -> {});

            assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Stops when priority holder has playable cards")
        void stopsWhenPriorityHolderHasPlayableCards() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            sut.resolveAutoPass(gd, ignored -> {});

            assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Auto-passes when priority holder has no playable cards")
        void autoPassesWhenNothingToPlay() {
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id, player2Id, (UUID) null);
            when(gameBroadcastService.getPlayableCardIndices(any(), any())).thenReturn(List.of());

            boolean[] advanceCalled = {false};
            sut.resolveAutoPass(gd, ignored -> {
                advanceCalled[0] = true;
                // Simulate step advancement ending the loop by finishing the game
                ignored.status = GameStatus.FINISHED;
            });

            assertThat(advanceCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Advances step when both players have passed priority")
        void advancesStepWhenBothPassed() {
            // First call: no priority holder (both already passed) → advance
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(null);

            boolean[] advanceCalled = {false};
            sut.resolveAutoPass(gd, ignored -> {
                advanceCalled[0] = true;
                ignored.status = GameStatus.FINISHED;
            });

            assertThat(advanceCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Stops at declare blockers for attacking player when blockers exist")
        void stopsAtDeclareBlockersWhenBlockersExist() {
            gd.currentStep = TurnStep.DECLARE_BLOCKERS;
            gd.activePlayerId = player1Id;

            Permanent blocker = createPermanent();
            blocker.setBlocking(true);
            gd.playerBattlefields.get(player2Id).add(blocker);

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());
            when(gameQueryService.getOpponentId(gd, player1Id)).thenReturn(player2Id);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Stops at auto-stop steps configured for the priority holder")
        void stopsAtAutoStopSteps() {
            gd.playerAutoStopSteps.put(player1Id, java.util.Set.of(TurnStep.PRECOMBAT_MAIN));

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());

            sut.resolveAutoPass(gd, ignored -> {});

            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Processes pending spell-target triggers before loop")
        void processesSpellTargetTriggers() {
            gd.pendingSpellTargetTriggers.add(
                    new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                            new Card(), player1Id, List.of(new DealDamageToAnyTargetEffect(1)), false));
            // After trigger processing, stop via finished status
            gd.status = GameStatus.RUNNING;
            org.mockito.Mockito.doAnswer(inv -> {
                gd.pendingSpellTargetTriggers.clear();
                gd.status = GameStatus.FINISHED;
                return null;
            }).when(triggerCollectionService).processNextSpellTargetTrigger(gd);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(triggerCollectionService).processNextSpellTargetTrigger(gd);
        }

        @Test
        @DisplayName("Processes pending discard self-triggers before loop")
        void processesDiscardSelfTriggers() {
            gd.pendingDiscardSelfTriggers.add(
                    new PermanentChoiceContext.DiscardTriggerAnyTarget(
                            new Card(), player1Id, List.of(new DealDamageToAnyTargetEffect(1))));
            org.mockito.Mockito.doAnswer(inv -> {
                gd.pendingDiscardSelfTriggers.clear();
                gd.status = GameStatus.FINISHED;
                return null;
            }).when(triggerCollectionService).processNextDiscardSelfTrigger(gd);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(triggerCollectionService).processNextDiscardSelfTrigger(gd);
        }

        @Test
        @DisplayName("Processes pending attack trigger targets before loop")
        void processesAttackTriggerTargets() {
            gd.pendingAttackTriggerTargets.add(
                    new PermanentChoiceContext.AttackTriggerTarget(
                            new Card(), player1Id, List.of(new DealDamageToAnyTargetEffect(1)), UUID.randomUUID()));
            org.mockito.Mockito.doAnswer(inv -> {
                gd.pendingAttackTriggerTargets.clear();
                gd.status = GameStatus.FINISHED;
                return null;
            }).when(triggerCollectionService).processNextAttackTriggerTarget(gd);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(triggerCollectionService).processNextAttackTriggerTarget(gd);
        }

        @Test
        @DisplayName("Processes pending death trigger targets before loop")
        void processesDeathTriggerTargets() {
            gd.pendingDeathTriggerTargets.add(
                    new PermanentChoiceContext.DeathTriggerTarget(
                            new Card(), player1Id, List.of(new DealDamageToAnyTargetEffect(1))));
            org.mockito.Mockito.doAnswer(inv -> {
                gd.pendingDeathTriggerTargets.clear();
                gd.status = GameStatus.FINISHED;
                return null;
            }).when(triggerCollectionService).processNextDeathTriggerTarget(gd);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(triggerCollectionService).processNextDeathTriggerTarget(gd);
        }

        @Test
        @DisplayName("Returns when awaiting input inside loop")
        void returnsWhenAwaitingInput() {
            gd.interaction.setAwaitingInput(AwaitingInput.PERMANENT_CHOICE);

            sut.resolveAutoPass(gd, ignored -> {});

            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("Stops at declare attackers when opponent forces attack and creatures available")
        void stopsAtDeclareAttackersWhenForcedToAttack() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            gd.activePlayerId = player1Id;

            // Player1 has an untapped creature that can attack
            Permanent creature = createPermanent();
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(creature);

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());
            when(combatAttackService.isOpponentForcedToAttack(gd, player1Id)).thenReturn(true);
            when(combatAttackService.getAttackableCreatureIndices(gd, player1Id)).thenReturn(List.of(0));

            sut.resolveAutoPass(gd, ignored -> {});

            // Should stop — active player must declare attackers
            verify(gameBroadcastService).broadcastGameState(gd);
            assertThat(gd.priorityPassedBy).doesNotContain(player1Id);
        }

        @Test
        @DisplayName("Auto-passes declare attackers when not forced to attack")
        void autoPassesDeclareAttackersWhenNotForced() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            gd.activePlayerId = player1Id;

            Permanent creature = createPermanent();
            creature.setSummoningSick(false);
            gd.playerBattlefields.get(player1Id).add(creature);

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id, player2Id);
            when(gameBroadcastService.getPlayableCardIndices(any(), any())).thenReturn(List.of());
            when(combatAttackService.isOpponentForcedToAttack(gd, player1Id)).thenReturn(false);

            boolean[] advanceCalled = {false};
            sut.resolveAutoPass(gd, ignored -> {
                advanceCalled[0] = true;
                ignored.status = GameStatus.FINISHED;
            });

            assertThat(advanceCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Auto-passes declare attackers when forced but attackers already declared")
        void autoPassesDeclareAttackersWhenForcedButAttackersDeclared() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            gd.activePlayerId = player1Id;

            // Player1 has a creature that is already attacking
            Permanent creature = createPermanent();
            creature.setSummoningSick(false);
            creature.setAttacking(true);
            gd.playerBattlefields.get(player1Id).add(creature);

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id, player2Id);
            when(gameBroadcastService.getPlayableCardIndices(any(), any())).thenReturn(List.of());

            boolean[] advanceCalled = {false};
            sut.resolveAutoPass(gd, ignored -> {
                advanceCalled[0] = true;
                ignored.status = GameStatus.FINISHED;
            });

            // Should auto-pass because attackers are already declared
            assertThat(advanceCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Auto-passes declare attackers when forced but no attackable creatures")
        void autoPassesDeclareAttackersWhenForcedButNoAttackableCreatures() {
            gd.currentStep = TurnStep.DECLARE_ATTACKERS;
            gd.activePlayerId = player1Id;

            // No creatures on battlefield
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id, player2Id);
            when(gameBroadcastService.getPlayableCardIndices(any(), any())).thenReturn(List.of());
            when(combatAttackService.isOpponentForcedToAttack(gd, player1Id)).thenReturn(true);
            when(combatAttackService.getAttackableCreatureIndices(gd, player1Id)).thenReturn(List.of());

            boolean[] advanceCalled = {false};
            sut.resolveAutoPass(gd, ignored -> {
                advanceCalled[0] = true;
                ignored.status = GameStatus.FINISHED;
            });

            // Should auto-pass because no creatures can attack (CR 508.1d)
            assertThat(advanceCalled[0]).isTrue();
        }

        @Test
        @DisplayName("Flushes pending mana-ability triggers to stack (CR 603.3)")
        void flushesPendingManaAbilityTriggers() {
            StackEntry pendingTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(), player2Id, "Viridian Revel trigger", List.of());
            gd.pendingManaAbilityTriggers.add(pendingTrigger);
            gd.priorityPassedBy.add(player1Id);

            // After flush, stack is non-empty so auto-pass stops
            sut.resolveAutoPass(gd, ignored -> {});

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getDescription()).isEqualTo("Viridian Revel trigger");
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            // Priority should be cleared so both players get a chance to respond
            assertThat(gd.priorityPassedBy).isEmpty();
            verify(gameBroadcastService).broadcastGameState(gd);
        }

        @Test
        @DisplayName("No-op when no pending mana-ability triggers")
        void noOpWhenNoPendingManaAbilityTriggers() {
            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            // Game finishes immediately so loop exits
            gd.status = GameStatus.FINISHED;

            sut.resolveAutoPass(gd, ignored -> {});

            assertThat(gd.pendingManaAbilityTriggers).isEmpty();
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Broadcasts once when second player has playable cards after first auto-passes")
        void broadcastsAfterSingleAutoPass() {
            // First call: player1 has priority, nothing to play → auto-pass
            // Second call: player2 has priority, has playable cards → stop
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id, player2Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());
            when(gameBroadcastService.getPlayableCardIndices(gd, player2Id)).thenReturn(List.of(0));

            sut.resolveAutoPass(gd, ignored -> {});

            // Player1 auto-passed, player2 was not auto-passed (has playable cards)
            assertThat(gd.priorityPassedBy).containsExactly(player1Id);
            // Single broadcast when player2 can act (no intermediate broadcast after player1's auto-pass)
            verify(gameBroadcastService, times(1)).broadcastGameState(gd);
        }
    }

    @Nested
    @DisplayName("hasInstantSpeedActivatedAbility")
    class HasInstantSpeedActivatedAbility {

        @Test
        @DisplayName("Returns false when battlefield is empty")
        void returnsFalseWhenBattlefieldEmpty() {
            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for card with no abilities")
        void returnsFalseForVanillaCreature() {
            Card card = new Card();
            Permanent perm = new Permanent(card);
            gd.playerBattlefields.get(player1Id).add(perm);

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for sorcery-speed activated ability")
        void returnsFalseForSorcerySpeedAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Test ability", ActivationTimingRestriction.SORCERY_SPEED));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for upkeep-only activated ability")
        void returnsFalseForUpkeepOnlyAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{6}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Untap", ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for mana ability")
        void returnsFalseForManaAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, null, List.of(new AwardAnyColorManaEffect()),
                            "Add one mana of any color"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for loyalty ability")
        void returnsFalseForLoyaltyAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(1, List.of(new DealDamageToAnyTargetEffect(3)), "Deal 3 damage"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false when tapped permanent has tap ability")
        void returnsFalseWhenTappedWithTapAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Deal 1 damage"));
            Permanent perm = new Permanent(card);
            perm.tap();
            gd.playerBattlefields.get(player1Id).add(perm);

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns true for untapped permanent with instant-speed tap ability")
        void returnsTrueForInstantSpeedAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Deal 1 damage"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Returns true for no-tap instant-speed ability")
        void returnsTrueForNoTapAbility() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(false, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Deal 1 damage"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isTrue();
        }

        @Test
        @DisplayName("Does not consider opponent's permanents")
        void doesNotConsiderOpponentPermanents() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Deal 1 damage"));
            gd.playerBattlefields.get(player2Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns false for attack-only ability when not attacking")
        void returnsFalseForAttackOnlyWhenNotAttacking() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(false, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Attack ability", ActivationTimingRestriction.ONLY_WHILE_ATTACKING));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isFalse();
        }

        @Test
        @DisplayName("Returns true for attack-only ability when permanent is attacking")
        void returnsTrueForAttackOnlyWhenAttacking() {
            Card card = createCardWithAbility(
                    new ActivatedAbility(false, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Attack ability", ActivationTimingRestriction.ONLY_WHILE_ATTACKING));
            Permanent perm = new Permanent(card);
            perm.setAttacking(true);
            gd.playerBattlefields.get(player1Id).add(perm);

            assertThat(sut.hasInstantSpeedActivatedAbility(gd, player1Id)).isTrue();
        }
    }

    @Nested
    @DisplayName("resolveAutoPassCombatTriggers")
    class ResolveAutoPassCombatTriggers {

        @Test
        @DisplayName("Returns immediately when stack is empty")
        void returnsImmediatelyWhenStackEmpty() {
            gd.stack.clear();

            sut.resolveAutoPassCombatTriggers(gd);

            assertThat(gd.stack).isEmpty();
            verify(stackResolutionService, never()).resolveTopOfStack(any());
        }

        @Test
        @DisplayName("Returns immediately when game is finished")
        void returnsImmediatelyWhenGameFinished() {
            gd.status = GameStatus.FINISHED;
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            sut.resolveAutoPassCombatTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            verify(stackResolutionService, never()).resolveTopOfStack(any());
        }

        @Test
        @DisplayName("Resolves stack when both players auto-pass")
        void resolvesStackWhenBothAutoPass() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            // Both players pass (null = both passed), then stack resolves and becomes empty
            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(null);
            // After resolveTopOfStack, simulate stack becoming empty
            org.mockito.stubbing.Answer<Void> clearStack = invocation -> {
                gd.stack.clear();
                return null;
            };
            org.mockito.Mockito.doAnswer(clearStack).when(stackResolutionService).resolveTopOfStack(gd);

            sut.resolveAutoPassCombatTriggers(gd);

            verify(stackResolutionService).resolveTopOfStack(gd);
        }

        @Test
        @DisplayName("Stops when priority holder has playable cards")
        void stopsWhenPriorityHolderHasPlayableCards() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of(0));

            sut.resolveAutoPassCombatTriggers(gd);

            verify(gameBroadcastService).broadcastGameState(gd);
            verify(stackResolutionService, never()).resolveTopOfStack(any());
        }

        @Test
        @DisplayName("Stops when priority holder has instant-speed activated ability")
        void stopsWhenPriorityHolderHasActivatedAbility() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            Card card = createCardWithAbility(
                    new ActivatedAbility(true, "{1}", List.of(new DealDamageToAnyTargetEffect(1)),
                            "Deal 1 damage"));
            gd.playerBattlefields.get(player1Id).add(new Permanent(card));

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(player1Id);
            when(gameBroadcastService.getPlayableCardIndices(gd, player1Id)).thenReturn(List.of());

            sut.resolveAutoPassCombatTriggers(gd);

            verify(gameBroadcastService).broadcastGameState(gd);
            verify(stackResolutionService, never()).resolveTopOfStack(any());
        }

        @Test
        @DisplayName("Returns when awaiting input")
        void returnsWhenAwaitingInput() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));
            gd.interaction.setAwaitingInput(AwaitingInput.PERMANENT_CHOICE);

            sut.resolveAutoPassCombatTriggers(gd);

            assertThat(gd.stack).hasSize(1);
            verify(stackResolutionService, never()).resolveTopOfStack(any());
        }

        @Test
        @DisplayName("Stops after resolution when pending may abilities exist")
        void stopsAfterResolutionWhenPendingMayAbilities() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(null);
            // After resolution, add a pending may ability and keep stack non-empty
            org.mockito.Mockito.doAnswer(inv -> {
                gd.pendingMayAbilities.add(
                        new PendingMayAbility(new Card(), player1Id, List.of(), "May draw a card"));
                return null;
            }).when(stackResolutionService).resolveTopOfStack(gd);

            sut.resolveAutoPassCombatTriggers(gd);

            verify(stackResolutionService).resolveTopOfStack(gd);
            assertThat(gd.pendingMayAbilities).hasSize(1);
        }

        @Test
        @DisplayName("Stops after resolution when awaiting input")
        void stopsAfterResolutionWhenAwaitingInput() {
            gd.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    new Card(),
                    player1Id,
                    "Test trigger",
                    List.of()
            ));

            when(gameQueryService.getPriorityPlayerId(gd)).thenReturn(null);
            org.mockito.Mockito.doAnswer(inv -> {
                gd.interaction.setAwaitingInput(AwaitingInput.PERMANENT_CHOICE);
                return null;
            }).when(stackResolutionService).resolveTopOfStack(gd);

            sut.resolveAutoPassCombatTriggers(gd);

            verify(stackResolutionService).resolveTopOfStack(gd);
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
        }
    }

    // ---- Test helpers ----

    private static Permanent createPermanent() {
        return new Permanent(new Card());
    }

    private static Card createCardWithAbility(ActivatedAbility ability) {
        Card card = new Card();
        card.getActivatedAbilities().add(ability);
        return card;
    }
}
