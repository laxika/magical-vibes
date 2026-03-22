package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageAmongTargetCreaturesEffect;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.testutil.FakeConnection;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HardAiDecisionEngineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
    }

    @Test
    @DisplayName("MCTS search completes within time budget for spell casting")
    void mctsSearchCompletesInTimeBudget() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        GameSimulator simulator = new GameSimulator(harness.getGameQueryService());
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 500);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(elapsed).isLessThan(3000); // Must complete within reasonable time
    }

    @Test
    @DisplayName("MCTS search completes within time budget for attacker declaration")
    void mctsSearchCompletesForAttackers() {
        // Add some creatures to battlefield
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        // Make them not summoning sick
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.forceActivePlayer(player1);
        gd.interaction.beginAttackerDeclaration(player1.getId());

        GameSimulator simulator = new GameSimulator(harness.getGameQueryService());
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 200);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.DeclareAttackers.class);
        assertThat(elapsed).isLessThan(3000);
    }

    @Test
    @DisplayName("HardAiDecisionEngine constructor initializes without errors")
    void hardEngineConstructorWorks() {
        HardAiDecisionEngine engine = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        assertThat(engine).isNotNull();
    }

    // ===== Sacrifice cost checks =====

    @Test
    @DisplayName("Hard AI skips spell with SacrificeArtifactCost when no artifact on battlefield")
    void skipsSpellWithSacrificeArtifactCostWhenNoArtifact() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of(new KuldothaRebirth()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no artifact to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    // ===== Sacrifice cost spell casting =====

    @Test
    @DisplayName("Hard AI casts Vivisection by sacrificing weakest creature")
    void castsVivisectionSacrificingWeakestCreature() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        for (int i = 0; i < 4; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island);
        }

        Permanent elves = new Permanent(new LlanowarElves()); // 1/1 — should be sacrificed
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 — should survive
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.setHand(player1, List.of(new Vivisection()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("Hard AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Give AI 2 Plains (land mana only)
        for (int i = 0; i < 2; i++) {
            Permanent plains = new Permanent(new com.github.laxika.magicalvibes.cards.p.Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(plains);
        }

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should NOT be on the stack — only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Add two Llanowar Elves (creature mana dorks)
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf2);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should be on the stack — creature mana is available from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    // ===== Must-attack =====

    @Test
    @DisplayName("Hard AI includes must-attack creature in attack declaration")
    void includesMustAttackCreature() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // AI has Berserkers of Blood Ridge (4/4 must-attack)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        assertThat(berserkers.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Hard AI blocker declaration does not leave game stuck")
    void blockerDeclarationDoesNotStick() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player2.getId(), "Bob");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player2, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        // Player1 attacks with Grizzly Bears
        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(humanBears);

        // AI has Air Elemental to block with
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(aiElemental);

        ai.handleMessage("AVAILABLE_BLOCKERS", "");

        // The blocker declaration should have been accepted
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== tryCastSpell silent failure recovery =====

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("tryCastSpell silent failure recovery")
    class TryCastSpellSilentFailureRecovery {

        @Mock private MessageHandler mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private Connection mockConnection;
        @Mock private GameBroadcastService mockGameBroadcastService;
        @Mock private com.github.laxika.magicalvibes.service.effect.TargetValidationService mockTargetValidationService;

        private GameData mockGd;
        private Player mockAiPlayer;
        private GameRegistry mockGameRegistry;

        @BeforeEach
        void setUpMocks() {
            UUID gameId = UUID.randomUUID();
            mockAiPlayer = new Player(UUID.randomUUID(), "AI");
            Player mockOpponent = new Player(UUID.randomUUID(), "Opponent");

            mockGd = new GameData(gameId, "test", mockAiPlayer.getId(), "AI");
            mockGd.status = GameStatus.RUNNING;
            mockGd.currentStep = TurnStep.PRECOMBAT_MAIN;
            mockGd.activePlayerId = mockAiPlayer.getId();
            mockGd.orderedPlayerIds.add(mockAiPlayer.getId());
            mockGd.orderedPlayerIds.add(mockOpponent.getId());
            mockGd.playerIdToName.put(mockAiPlayer.getId(), "AI");
            mockGd.playerIdToName.put(mockOpponent.getId(), "Opponent");
            mockGd.playerHands.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerHands.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerBattlefields.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerBattlefields.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerManaPools.put(mockAiPlayer.getId(), new ManaPool());
            mockGd.playerManaPools.put(mockOpponent.getId(), new ManaPool());
            mockGd.playerLifeTotals.put(mockAiPlayer.getId(), 20);
            mockGd.playerLifeTotals.put(mockOpponent.getId(), 20);
            mockGd.playerDecks.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerDecks.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerGraveyards.put(mockAiPlayer.getId(), Collections.synchronizedList(new ArrayList<>()));
            mockGd.playerGraveyards.put(mockOpponent.getId(), Collections.synchronizedList(new ArrayList<>()));

            mockGameRegistry = new GameRegistry();
            mockGameRegistry.register(mockGd);
        }

        private HardAiDecisionEngine createEngine() {
            Mockito.when(mockGameBroadcastService.isSpellCastingAllowed(any(), any(), any())).thenReturn(true);
            HardAiDecisionEngine engine = new HardAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockCombatAttackService, mockGameBroadcastService,
                    mockTargetValidationService);
            engine.setSelfConnection(mockConnection);
            return engine;
        }

        @Test
        @DisplayName("Hard AI passes priority when spell cast is silently rejected")
        void passesPriorityWhenSpellCastSilentlyRejected() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler).handlePassPriority(any(), any());
        }

        @Test
        @DisplayName("Hard AI does NOT pass priority when spell cast succeeds")
        void doesNotPassPriorityWhenSpellCastSucceeds() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any(), any());

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler, never()).handlePassPriority(any(), any());
        }

        @Test
        @DisplayName("Hard AI builds damage assignments for divided damage spell")
        void buildsDamageAssignmentsForDividedDamageSpell() throws Exception {
            Card spell = new Card();
            spell.setName("Test Divided Damage");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{1}{R}");
            spell.target(null, 1, 3)
                    .addEffect(EffectSlot.SPELL, new DealDividedDamageAmongTargetCreaturesEffect(3));
            mockGd.playerHands.get(mockAiPlayer.getId()).add(spell);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.RED, 1);
            pool.add(ManaColor.COLORLESS, 1);

            UUID opponentId = mockGd.orderedPlayerIds.get(1);
            Card creatureCard = new Card();
            creatureCard.setName("Opponent Creature");
            creatureCard.setType(CardType.CREATURE);
            creatureCard.setPower(2);
            creatureCard.setToughness(2);
            Permanent creature = new Permanent(creatureCard);
            mockGd.playerBattlefields.get(opponentId).add(creature);

            when(mockGameQueryService.isCreature(mockGd, creature)).thenReturn(true);
            when(mockGameQueryService.getEffectiveToughness(mockGd, creature)).thenReturn(2);
            when(mockTargetValidationService.checkEffectTargets(any(), any())).thenReturn(Optional.empty());

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any(), any());

            createEngine().handleMessage("GAME_STATE", "");

            ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
            verify(mockMessageHandler).handlePlayCard(eq(mockConnection), captor.capture());

            PlayCardRequest request = captor.getValue();
            assertThat(request.damageAssignments()).isNotNull();
            assertThat(request.damageAssignments()).containsEntry(creature.getId(), 3);
        }
    }

    // ===== Modal spell handling (ChooseOneEffect) =====

    @Test
    @DisplayName("Hard AI does not cast Steel Sabotage when no mode has valid targets")
    void doesNotCastSteelSabotageWhenNoValidMode() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        Permanent island = new Permanent(new Island());
        island.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(island);

        harness.setHand(player1, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI skips Steel Sabotage (no valid mode) and casts another available spell")
    void skipsModalSpellAndCastsAlternative() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // 2 Plains for Pacifism ({1}{W})
        for (int i = 0; i < 2; i++) {
            Permanent plains = new Permanent(new com.github.laxika.magicalvibes.cards.p.Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(plains);
        }

        // Opponent has a creature (Pacifism target)
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        // Steel Sabotage has no valid mode (no artifacts), but Pacifism is castable
        harness.setHand(player1, List.of(new SteelSabotage(), new com.github.laxika.magicalvibes.cards.p.Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip Steel Sabotage and cast Pacifism
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Hard AI casts Steel Sabotage to bounce artifact creature on opponent's battlefield")
    void castsSteelSabotageToBounceArtifact() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        // Set up as opponent's turn, end step — good timing for REMOVAL instants
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        Permanent island = new Permanent(new Island());
        island.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(island);

        // Artifact creature so bounce evaluator gives positive value (creature score)
        Card artifactCreature = new Card();
        artifactCreature.setName("Test Artifact Creature");
        artifactCreature.setType(CardType.ARTIFACT);
        artifactCreature.setAdditionalTypes(Set.of(CardType.CREATURE));
        artifactCreature.setPower(3);
        artifactCreature.setToughness(3);
        Permanent artifactPerm = new Permanent(artifactCreature);
        gd.playerBattlefields.get(player2.getId()).add(artifactPerm);

        harness.setHand(player1, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Steel Sabotage");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifactPerm.getId());
    }

    @Test
    @DisplayName("Hard AI casts Slagstorm to wipe opponent's creatures")
    void castsSlagstorm() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        for (int i = 0; i < 3; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(mountain);
        }

        // Opponent has creatures so board wipe evaluator gives positive value
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Slagstorm()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slagstorm");
    }
}
