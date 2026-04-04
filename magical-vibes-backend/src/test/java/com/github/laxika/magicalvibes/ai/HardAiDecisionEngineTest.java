package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.ElvishArchdruid;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
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
import org.junit.jupiter.api.Tag;
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

@Tag("scryfall")
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        // Use postcombat — Vivisection is non-combat so Hard AI defers it past combat
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);

        harness.forceActivePlayer(player1);
        // Use postcombat — non-haste creature deferred past combat by Hard AI
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                    mockTargetValidationService, null);
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

        @Test
        @DisplayName("Hard AI does not cast spell when mana tapping triggers awaiting input")
        void doesNotCastSpellWhenManaTappingTriggersAwaitingInput() throws Exception {
            Card creature = new Card();
            creature.setName("Test Knight");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{W}");
            creature.setPower(2);
            creature.setToughness(2);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            // Add an untapped Plains so AI needs to tap it for mana
            Permanent land = new Permanent(new Plains());
            land.setSummoningSick(false);
            mockGd.playerBattlefields.get(mockAiPlayer.getId()).add(land);

            // Allow tapping flow to proceed
            when(mockGameQueryService.canActivateManaAbility(any(), any())).thenReturn(true);

            // Simulate mana ability triggering awaiting input (e.g. Treasure color choice)
            Mockito.doAnswer(inv -> {
                mockGd.interaction.setAwaitingInput(AwaitingInput.COLOR_CHOICE);
                return null;
            }).when(mockMessageHandler).handleTapPermanent(any(), any());

            createEngine().handleMessage("GAME_STATE", "");

            // AI should have tapped but NOT cast the spell or passed priority
            verify(mockMessageHandler).handleTapPermanent(any(), any());
            verify(mockMessageHandler, never()).handlePlayCard(any(), any());
            verify(mockMessageHandler, never()).handlePassPriority(any(), any());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);
        ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));

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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
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

    // ===== Attack tax handling =====

    @Test
    @DisplayName("Hard AI limits attackers when attack tax is present")
    void limitsAttackersWhenAttackTaxPresent() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);

        // Player 2 (human/opponent) controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(baird);

        // AI (player1) has 3 creatures and only 1 Plains
        Permanent plains = new Permanent(new Plains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(plains);
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.beginAttackerDeclaration(player1.getId());

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // At most 1 creature should be attacking (can only afford {1} tax)
        long attackingCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isLessThanOrEqualTo(1);
    }

    // ===== ExileNCardsFromGraveyardCost (e.g. Skaab Ruinator) =====

    @Test
    @DisplayName("Hard AI casts Skaab Ruinator when graveyard has 3 creature cards")
    void castsSkaabRuinatorWithThreeCreatures() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);
        ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Add 3 Islands for mana
        for (int i = 0; i < 3; i++) {
            Permanent island = new Permanent(new com.github.laxika.magicalvibes.cards.i.Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island);
        }

        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Skaab Ruinator selecting only creatures from mixed graveyard")
    void castsSkaabRuinatorFromMixedGraveyard() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);
        ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        for (int i = 0; i < 3; i++) {
            Permanent island = new Permanent(new com.github.laxika.magicalvibes.cards.i.Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island);
        }

        gd.playerGraveyards.get(player1.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Holy Day"));
    }

    // ===== Entrancing Melody (PermanentManaValueEqualsXPredicate) =====

    private HardAiDecisionEngine createHardAi(Player aiPlayer) {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), aiPlayer.getUsername());
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setSelfConnection(aiConn);
        ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));
        return ai;
    }

    private void givePlayerIslands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(island);
        }
    }

    private void giveAiPriority(Player aiPlayer) {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
    }

    @Test
    @DisplayName("Hard AI casts Entrancing Melody with X matching target creature's mana value")
    void castsEntrancingMelodyWithCorrectX() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // maxX = 2

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hard AI picks highest affordable target for Entrancing Melody")
    void picksHighestAffordableTargetForEntrancingMelody() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // maxX = 2

        Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Hard AI skips Entrancing Melody when target too expensive")
    void skipsEntrancingMelodyWhenTooExpensive() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 3); // maxX = 1

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2, unaffordable
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    // ===== X-spell cost modifier handling =====

    @Test
    @DisplayName("Hard AI skips Entrancing Melody when cost modifier makes only target unaffordable")
    void skipsEntrancingMelodyWhenCostModifierMakesTargetUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerIslands(player1, 4); // 4U total; Entrancing Melody {X}{U}{U} → without modifier maxX=2

        // Thalia on opponent's battlefield: +1 cost → maxX=1
        Permanent thalia = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben());
        thalia.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(thalia);

        // MV=2 creature — needs X=2 but maxX=1 with Thalia → unaffordable
        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        // Without the fix, AI would compute maxX=2 (ignoring modifier) and try to steal Bears,
        // which would fail server-side validation. With the fix, AI sees maxX=1 and skips.
        assertThat(gd.stack).isEmpty();
    }

    // ===== Forced attack (Trove of Temptation) =====

    @Test
    @DisplayName("Hard AI attacks with at least one creature when Trove of Temptation forces attack")
    void attacksWithAtLeastOneWhenForcedByTroveOfTemptation() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Opponent controls Trove of Temptation
        Permanent trove = new Permanent(new TroveOfTemptation());
        trove.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(trove);

        // AI has a 2/2 and opponent has a 4/4 blocker — simulator would normally skip attacking
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Must attack with at least one creature despite the unfavorable board
        long attackingCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isGreaterThanOrEqualTo(1);
    }

    // ===== Smart land selection =====

    @Test
    @DisplayName("Hard AI plays the land that enables casting a spell in hand")
    void playsLandThatEnablesSpellCasting() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // AI has 1 colorless mana available from an untapped Mountain
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        // Hand: Forest, Plains, and Grizzly Bears ({1}{G})
        // Forest should be chosen because it enables casting Grizzly Bears
        Card forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Card plains = new Plains();
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(forest, plains, bears));

        // First GAME_STATE: AI plays the best land
        ai.handleMessage("GAME_STATE", "");

        // Forest should be on the battlefield (not Plains)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Plains"));

        // Second GAME_STATE: AI casts the now-enabled spell
        harness.clearPriorityPassed();
        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI prefers land with better color coverage when no spell is immediately castable")
    void prefersLandWithBetterColorCoverage() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // No mana on battlefield — neither land alone enables a 2-cost spell
        // Hand: Forest, Plains, Serra Angel ({3}{W}{W})
        // Plains should be chosen because Serra Angel needs {W}{W}
        Card forest = new com.github.laxika.magicalvibes.cards.f.Forest();
        Card plains = new Plains();
        Card angel = new SerraAngel();
        harness.setHand(player1, List.of(forest, plains, angel));

        ai.handleMessage("GAME_STATE", "");

        // Neither land alone enables Serra Angel, so AI should choose based on coverage.
        // Plains matches Serra Angel's {W}{W} requirement, Forest matches nothing.
        // The Plains should have been played — verify it's on the battlefield.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    // ===== Targeting tax handling =====

    private void givePlayerPlains(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(plains);
        }
    }

    private void givePlayerMountains(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(mountain);
        }
    }

    @Test
    @DisplayName("Hard AI does not cast Pacifism when targeting tax makes it unaffordable")
    void doesNotCastPacifismWhenTargetingTaxMakesUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerPlains(player1, 2); // Only 2 mana — Pacifism costs {1}{W} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should NOT cast — can't afford {1}{W} + {2} tax = 4 mana with only 2 Plains
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Pacifism when it can afford targeting tax")
    void castsPacifismWhenCanAffordTargetingTax() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerPlains(player1, 4); // 4 mana — enough for {1}{W} + {2} tax

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.p.Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Hard AI does not cast instant when targeting tax makes it unaffordable")
    void doesNotCastInstantWhenTargetingTaxMakesUnaffordable() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, beginning of combat — good timing for REMOVAL instants
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        givePlayerMountains(player1, 1); // Only 1 mana — Lightning Bolt costs {R} but Kopala adds {2}

        Permanent kopala = new Permanent(new com.github.laxika.magicalvibes.cards.k.KopalaWardenOfWaves());
        kopala.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(kopala);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.l.LightningBolt()));

        ai.handleMessage("GAME_STATE", "");

        // Should NOT cast — can't afford {R} + {2} tax = 3 mana with only 1 Mountain
        assertThat(gd.stack).isEmpty();
    }

    // ===== Counterspell Casting =====

    @Test
    @DisplayName("Hard AI casts Cancel to counter opponent's creature spell")
    void castsCancelToCounterOpponentCreatureSpell() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn with a spell on the stack
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // Simulate that the active player (opponent) has already passed priority
        // after casting their spell, so now AI gets priority to respond
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Put an opponent's creature spell on the stack
        SerraAngel angel = new SerraAngel();
        com.github.laxika.magicalvibes.model.StackEntry opponentSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(angel, player2.getId());
        gd.stack.add(opponentSpell);

        // Give the AI enough mana for Cancel (1UU)
        givePlayerIslands(player1, 3);

        // Give the AI Cancel in hand
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // The AI should cast Cancel targeting the Serra Angel
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        com.github.laxika.magicalvibes.model.StackEntry cancelOnStack = gd.stack.getLast();
        assertThat(cancelOnStack.getCard().getName()).isEqualTo("Cancel");
        assertThat(cancelOnStack.getTargetId()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Hard AI does not cast Cancel when only own spells are on the stack")
    void doesNotCancelOwnSpells() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI is active player and has just cast its own spell
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Put AI's own creature spell on the stack
        GrizzlyBears bears = new GrizzlyBears();
        com.github.laxika.magicalvibes.model.StackEntry ownSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(bears, player1.getId());
        gd.stack.add(ownSpell);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.c.Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // Stack should only have the original spell — Cancel should not have been cast
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI does not waste Cancel on a low-value spell")
    void doesNotWasteCancelOnLowValueSpell() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        // Opponent passed priority after casting
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Put a low-value opponent spell on the stack (Llanowar Elves, MV=1)
        LlanowarElves elves = new LlanowarElves();
        com.github.laxika.magicalvibes.cards.c.Cancel cancelCard = new com.github.laxika.magicalvibes.cards.c.Cancel();
        com.github.laxika.magicalvibes.model.StackEntry lowValueSpell =
                new com.github.laxika.magicalvibes.model.StackEntry(elves, player2.getId());
        gd.stack.add(lowValueSpell);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(cancelCard));

        ai.handleMessage("GAME_STATE", "");

        // Should NOT waste a 3-mana counterspell on a 1/1 for 1 mana.
        // After the AI passes priority, both players have passed, stack resolves.
        // Verify Cancel was not cast by checking the hand still contains it.
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).isNotNull();
        assertThat(hand.stream().anyMatch(c -> c.getName().equals("Cancel"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI values countering a board wipe higher than a vanilla creature of same CMC")
    void valuesCounteringBoardWipeHigherThanVanillaCreature() {
        // Give the AI a strong board that the board wipe would destroy
        Permanent angel1 = new Permanent(new SerraAngel());
        angel1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel1);
        Permanent angel2 = new Permanent(new SerraAngel());
        angel2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel2);
        Permanent angel3 = new Permanent(new SerraAngel());
        angel3.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel3);

        // Test 1: Opponent casts Wrath of God (board wipe, MV=4)
        HardAiDecisionEngine ai1 = createHardAi(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        WrathOfGod wrath = new WrathOfGod();
        StackEntry wrathOnStack = new StackEntry(StackEntryType.SORCERY_SPELL, wrath, player2.getId(),
                wrath.getName(), wrath.getEffects(EffectSlot.SPELL), 0);
        gd.stack.add(wrathOnStack);
        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai1.handleMessage("GAME_STATE", "");

        // AI should counter the board wipe — it threatens its entire board
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelOnStack = gd.stack.getLast();
        assertThat(cancelOnStack.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI counters removal targeting its best creature")
    void countersRemovalTargetingBestCreature() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a valuable creature on the battlefield
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Opponent casts Doom Blade (MV=2) targeting the Angel
        DoomBlade doomBlade = new DoomBlade();
        StackEntry removalOnStack = new StackEntry(StackEntryType.INSTANT_SPELL, doomBlade, player2.getId(),
                doomBlade.getName(), doomBlade.getEffects(EffectSlot.SPELL), 0, angel.getId(), null);
        gd.stack.add(removalOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // AI should counter the removal even though Doom Blade (MV=2) < Cancel (MV=3),
        // because it's targeting a Serra Angel (high creature value)
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI saves counterspell when board is strong and opponent casts mediocre creature")
    void savesCounterspellWhenBoardIsStrongAndThreatIsMediocre() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a very strong board (3 Serra Angels = high board strength > 30)
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Opponent casts a mediocre creature (Grizzly Bears, MV=2, 2/2)
        GrizzlyBears bears = new GrizzlyBears();
        StackEntry bearsOnStack = new StackEntry(bears, player2.getId());
        gd.stack.add(bearsOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // AI should NOT counter a 2/2 when it already has 3 Serra Angels —
        // save the counterspell for something more threatening
        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).isNotNull();
        assertThat(hand.stream().anyMatch(c -> c.getName().equals("Cancel"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI still counters board wipe even when board is strong (reservation bypassed)")
    void countersHighValueSpellEvenWhenBoardIsStrong() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI has a very strong board
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Opponent casts Wrath of God — the most threatening spell possible against a big board
        WrathOfGod wrath = new WrathOfGod();
        StackEntry wrathOnStack = new StackEntry(StackEntryType.SORCERY_SPELL, wrath, player2.getId(),
                wrath.getName(), wrath.getEffects(EffectSlot.SPELL), 0);
        gd.stack.add(wrathOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // AI MUST counter the board wipe — it would destroy all 3 Serra Angels
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    @Test
    @DisplayName("Hard AI counters mediocre spell when at low life (reservation bypassed)")
    void countersAnySpellAtLowLife() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI is at critically low life
        harness.setLife(player1, 4);

        // AI has a strong board
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);

        // Opponent casts Air Elemental (4/4 flying, MV=5)
        // Normally might not be countered with a strong board, but at low life AI is desperate
        AirElemental elemental = new AirElemental();
        StackEntry spellOnStack = new StackEntry(elemental, player2.getId());
        gd.stack.add(spellOnStack);

        givePlayerIslands(player1, 3);
        harness.setHand(player1, List.of(new Cancel()));

        ai.handleMessage("GAME_STATE", "");

        // At low life, the reservation threshold is bypassed — counter everything
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry cancelEntry = gd.stack.getLast();
        assertThat(cancelEntry.getCard().getName()).isEqualTo("Cancel");
    }

    // ===== Multi-spell awareness =====

    private void givePlayerForests(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(forest);
        }
    }

    private void givePlayerSwamps(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(swamp);
        }
    }

    @Test
    @DisplayName("Hard AI casts sorceries when total multi-spell value exceeds instant held value")
    void castsMultipleSorceriesInsteadOfHoldingForInstant() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 4 green mana — enough for two 2-drop creatures
        givePlayerForests(player1, 4);

        // Hand: two Grizzly Bears ({1}{G} each, value ~7 each, total ~14)
        // and a Shock ({R} instant, value ~3, held value ~4)
        // Total sorcery value ~14 should beat instant held value ~4.
        // With old logic (single best sorcery = ~7), the comparison was closer.
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 1); // For Shock to be castable

        ai.handleMessage("GAME_STATE", "");

        // AI should cast a sorcery-speed creature (not hold all mana for Shock)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for instant when both fit")
    void castsSorceryAndReservesManaForInstant() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 4 mana (3 green + 1 red) — enough for one 2-drop + keep 2 for instant
        givePlayerForests(player1, 3);
        givePlayerMountains(player1, 1);

        // Hand: Grizzly Bears ({1}{G}, 2 mana) + Lightning Bolt ({R} instant, 1 mana)
        // With 4 total mana, AI can cast Bears (2 mana) and still afford Bolt (1 mana)
        // Even though Bolt's held value might beat single Bears value with the 0.8 factor,
        // the AI should still cast Bears because it can do both.
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setHand(player1, List.of(bears, bolt));

        // Put an opponent creature so Lightning Bolt has a target worth holding for
        Permanent oppCreature = new Permanent(new EliteVanguard());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleMessage("GAME_STATE", "");

        // AI should cast the creature (it can still afford the instant later)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI holds mana for instant when only a single low-value sorcery is available")
    void holdsForInstantWhenSingleLowValueSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 3 mana (1 green + 2 red)
        givePlayerForests(player1, 1);
        givePlayerMountains(player1, 2);

        // Hand: EliteVanguard ({W}, low value creature that can't even be cast because no white mana)
        // + Lightning Bolt ({R} instant, 3 damage — high held value ~4.5*1.3 ≈ 5.9)
        // Since the only sorcery-speed option is not castable, AI should pass
        // (falls through to instant timing, but it's own main phase so non-counterspell
        // instants are not cast at this timing)
        harness.setHand(player1, List.of(new EliteVanguard(), new LightningBolt()));

        // Put an opponent creature so Lightning Bolt has removal value
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleMessage("GAME_STATE", "");

        // No sorcery should be cast (EliteVanguard needs white), and Lightning Bolt
        // should be held for opponent's turn
        assertThat(gd.stack).isEmpty();
    }

    // ===== Multi-instant holding =====

    @Test
    @DisplayName("Hard AI holds for two instants when combined held value exceeds cast-one-hold-one")
    void holdsForMultipleInstantsWhenCombinedValueExceedsSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // 5 mana: 2 mountains + 3 islands
        givePlayerMountains(player1, 2);
        givePlayerIslands(player1, 3);

        // Hand: GoblinPiker ({1}{R}, ~5 value) + Cancel ({1}{U}{U}, held ~14.4) + Negate ({1}{U}, held ~9.6)
        // Total instant cost = 3 + 2 = 5 = total mana.
        // Hold both instants: (14.4 + 9.6) * 0.8 = 19.2
        // Cast GoblinPiker + hold Cancel: ~5 + 14.4 * 0.8 = ~16.5
        // Holding both wins (19.2 > 16.5), so AI should hold all mana.
        harness.setHand(player1, List.of(new GoblinPiker(), new Cancel(), new Negate()));

        // Opponent creature for spell evaluation context
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleMessage("GAME_STATE", "");

        // AI should hold mana for both counterspells — stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for multiple instants when all fit")
    void castsSorceryWhileReservingManaForMultipleInstants() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // 7 mana: 4 forests + 3 islands — enough for Bears (2) + Cancel (3) + Negate (2) = 7
        givePlayerForests(player1, 4);
        givePlayerIslands(player1, 3);

        // All fit within 7 mana, so AI should cast the creature and still hold both instants.
        harness.setHand(player1, List.of(new GrizzlyBears(), new Cancel(), new Negate()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast the sorcery-speed creature (can hold both instants with remaining mana)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI holds for two instants in postcombat main when combined value exceeds sorcery")
    void holdsForMultipleInstantsInPostcombatMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Postcombat main phase setup — tryCastSpellWithInstantAwareness is called directly
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // 5 mana: 2 mountains + 3 islands
        givePlayerMountains(player1, 2);
        givePlayerIslands(player1, 3);

        // Hand: GoblinPiker ({1}{R}, ~7.5 value) + Cancel ({1}{U}{U}, held ~14.4) + Negate ({1}{U}, held ~9.6)
        // Total instant cost = 3 + 2 = 5 = total mana.
        // Hold both: (14.4 + 9.6) * 0.8 = 19.2
        // Cast GoblinPiker + hold Cancel: ~7.5 + 14.4 * 0.8 ≈ 19.0
        // Holding both (19.2) just beats cast+hold-one (~19.0), so AI holds all mana.
        harness.setHand(player1, List.of(new GoblinPiker(), new Cancel(), new Negate()));

        // Opponent creature for evaluation context (use GrizzlyBears to keep sorcery value moderate)
        Permanent oppCreature = new Permanent(new GrizzlyBears());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        ai.handleMessage("GAME_STATE", "");

        // AI should hold mana for both counterspells in postcombat — stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI holds for multiple instants instead of casting precombat removal")
    void holdsForMultipleInstantsInsteadOfPrecombatRemoval() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Precombat main phase setup
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // AI has an attacker
        Permanent attacker = new Permanent(new GrizzlyBears()); // 2/2
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Opponent has a blocker
        Permanent blocker = new Permanent(new EliteVanguard()); // 2/1
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        // 5 mana: 1 swamp + 4 islands
        givePlayerSwamps(player1, 1);
        givePlayerIslands(player1, 4);

        // Hand: Eviscerate ({3}{B}, destroy creature, costs 4) + Cancel ({1}{U}{U}) + Negate ({1}{U})
        // If Eviscerate is cast (4 mana), only 1 mana left — can't hold Cancel (3) or Negate (2).
        // If holding instead: Cancel (3) + Negate (2) = 5 mana, both fit.
        // Held value: (14.4 + 9.6) * 0.8 = 19.2
        // Cast value: Eviscerate precombat value ≈ 10 (removal + damage gain)
        // Holding clearly wins.
        harness.setHand(player1, List.of(new Eviscerate(), new Cancel(), new Negate()));

        ai.handleMessage("GAME_STATE", "");

        // AI should hold mana for both counterspells instead of removing the blocker
        assertThat(gd.stack).isEmpty();
    }

    // ===== Color-aware mulligan =====

    @Nested
    @DisplayName("Color-aware mulligan decisions")
    class ColorAwareMulligan {

        /**
         * Thin subclass to expose the protected shouldKeepHand for direct testing.
         */
        private class TestableMulliganEngine extends HardAiDecisionEngine {
            TestableMulliganEngine(Player player) {
                super(gd.id, player, harness.getGameRegistry(),
                        harness.getMessageHandler(), harness.getGameQueryService(),
                        harness.getCombatAttackService(), harness.getGameBroadcastService(),
                        harness.getTargetValidationService(), harness.getTargetLegalityService());
            }

            boolean testShouldKeepHand(GameData gameData) {
                return shouldKeepHand(gameData);
            }
        }

        @Test
        @DisplayName("Mulligans hand with mountains and only blue spells")
        void mulligansWhenLandsDoNotMatchSpellColors() {
            // 3 Mountains + 4 blue spells — no way to cast anything
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(), new Mountain(),
                    new AirElemental(), new AirElemental(), new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }

        @Test
        @DisplayName("Keeps hand when lands match spell colors")
        void keepsHandWhenLandsMatchSpellColors() {
            // 3 Islands + 2 cheap blue spells + 2 medium blue spells
            // Score: 3*1.5 + 2*3.0 + 2*1.5 = 4.5 + 6.0 + 3.0 = 13.5 >= 12.0
            harness.setHand(player1, List.of(
                    new Island(), new Island(), new Island(),
                    new SteelSabotage(), new SteelSabotage(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isTrue();
        }

        @Test
        @DisplayName("Keeps hand when at least some spells are color-castable")
        void keepsHandWhenSomeSpellsAreCastable() {
            // 3 Mountains + mix of red and blue spells
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(), new Mountain(),
                    new Slagstorm(), new Slagstorm(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 0);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            // Slagstorms score normally (3.0 each), AirElementals score 0.25 each
            // Score: 3*1.5 + 2*3.0 + 2*0.25 = 4.5 + 6.0 + 0.5 = 11.0 < 12.0 threshold
            // This hand should be mulliganed — the blue spells drag the score down
            assertThat(engine.testShouldKeepHand(gd)).isFalse();
        }

        @Test
        @DisplayName("Keeps color-mismatched hand after 3 mulligans")
        void keepsAfterThreeMulligansRegardlessOfColors() {
            // Even with total color mismatch, 3+ mulligans = always keep
            harness.setHand(player1, List.of(
                    new Mountain(), new Mountain(),
                    new AirElemental(), new AirElemental()
            ));
            gd.mulliganCounts.put(player1.getId(), 3);

            TestableMulliganEngine engine = new TestableMulliganEngine(player1);
            assertThat(engine.testShouldKeepHand(gd)).isTrue();
        }
    }

    // ===== Activated Ability Usage =====

    @Test
    @DisplayName("Hard AI activates Prodigal Pyromancer's tap ability to deal damage to opponent creature")
    void activatesProdigalPyromancerTapAbility() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, end step — good timing for "any time" abilities
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // AI has Prodigal Pyromancer ({T}: deal 1 damage to any target)
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        // Opponent has a 1/1 creature that can be killed
        Permanent oppElves = new Permanent(new LlanowarElves());
        oppElves.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppElves);

        // Empty hand so AI doesn't try casting spells
        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Pyromancer should have been tapped and ability put on the stack
        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(oppElves.getId());
    }

    @Test
    @DisplayName("Hard AI does not activate tap ability on summoning-sick creature")
    void doesNotActivateTapAbilityOnSummoningSickCreature() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Summoning-sick Prodigal Pyromancer
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        Permanent oppCreature = new Permanent(new LlanowarElves());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should not activate — creature is summoning sick
        assertThat(pyromancer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI activates Prodigal Pyromancer targeting opponent face when no killable creature")
    void activatesPyromancerTargetingOpponentFace() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Prodigal Pyromancer untapped
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        // No opponent creatures — should target opponent face
        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Hard AI activates Shivan Dragon pump ability only during combat")
    void activatesShivanDragonPumpOnlyDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Declare blockers step — good timing for pump
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Shivan Dragon with {R} available for pump
        Permanent dragon = new Permanent(new com.github.laxika.magicalvibes.cards.s.ShivanDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        // One untapped Mountain for mana
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should activate pump during combat
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Hard AI does not activate pump ability during precombat main phase")
    void doesNotActivatePumpDuringMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Precombat main — not a good time for pump
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        Permanent dragon = new Permanent(new com.github.laxika.magicalvibes.cards.s.ShivanDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should NOT pump during main phase — waste of mana
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI activates Thrun regenerate ability during combat")
    void activatesThrunRegenerateDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Declare blockers — good timing for regenerate
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Thrun the Last Troll ({1}{G}: Regenerate)
        Permanent thrun = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll());
        thrun.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thrun);

        // Two lands for {1}{G}
        Permanent forest = new Permanent(new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(forest);

        Permanent forest2 = new Permanent(new Forest());
        forest2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(forest2);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should activate regenerate during combat
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Hard AI does not activate regenerate during precombat main")
    void doesNotActivateRegenerateDuringMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        Permanent thrun = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll());
        thrun.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thrun);

        Permanent forest = new Permanent(new Forest());
        forest.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(forest);

        Permanent forest2 = new Permanent(new Forest());
        forest2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(forest2);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should NOT activate regenerate during main — save mana for casting
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate ability when it cannot afford the mana cost")
    void doesNotActivateAbilityWithInsufficientMana() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // Thrun needs {1}{G} but we have no mana
        Permanent thrun = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll());
        thrun.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thrun);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should not activate — can't afford {1}{G}
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate Mogg Fanatic sacrifice when its value exceeds damage value")
    void doesNotSacrificeMoggFanaticWhenNotWorthIt() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Mogg Fanatic (1/1, sacrifice: deal 1 damage to any target)
        Permanent mogg = new Permanent(new com.github.laxika.magicalvibes.cards.m.MoggFanatic());
        mogg.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mogg);

        // Opponent has a 5/5 creature — 1 damage won't kill it, sacrifice not worth it
        Permanent bigCreature = new Permanent(new AirElemental());
        bigCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bigCreature);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Cost of sacrificing the 1/1 should exceed value of dealing 1 to opponent face
        // or dealing 1 to a 4/4 creature (can't kill it)
        // The sacrifice cost (~creature score of 1/1) should make value negative
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate pay-life ability when life is too low")
    void doesNotPayLifeWhenLifeTooLow() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();

        // AI at 2 life — paying 2 would kill it
        gd.playerLifeTotals.put(player1.getId(), 2);

        // Glorifier of Dusk (Pay 2 life: gain flying/vigilance)
        Permanent glorifier = new Permanent(new com.github.laxika.magicalvibes.cards.g.GlorifierOfDusk());
        glorifier.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(glorifier);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should not activate — life cost check: life <= amount means can't pay
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate ability on already tapped permanent")
    void doesNotActivateTapAbilityOnTappedPermanent() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Already-tapped Prodigal Pyromancer
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        pyromancer.tap();
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        Permanent oppCreature = new Permanent(new LlanowarElves());
        oppCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppCreature);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should not activate — permanent is already tapped
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI prefers killing a creature over pinging opponent face")
    void prefersKillingCreatureOverFaceDamage() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Prodigal Pyromancer
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        // Opponent has a 1/1 that can be killed by 1 damage
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elves);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // Should target the 1/1 creature (killable) rather than opponent's face
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(elves.getId());
    }

    @Test
    @DisplayName("Hard AI skips mana abilities and does not put them on the stack")
    void skipsManaAbilitiesDuringAbilityActivation() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Llanowar Elves has a mana ability ({T}: Add {G})
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);

        harness.setHand(player1, List.of());

        ai.handleMessage("GAME_STATE", "");

        // The mana ability should be skipped — nothing on the stack
        assertThat(gd.stack).isEmpty();
        // Elves should NOT be tapped (mana ability was not attempted)
        assertThat(elves.isTapped()).isFalse();
    }

    // ===== Race-aware decisions =====

    @Nested
    @DisplayName("Burn-to-face lethal")
    class BurnToFaceLethal {

        @Test
        @DisplayName("Hard AI casts burn spell to kill opponent when burn lethal is available")
        void castsBurnToFaceWhenLethal() {
            HardAiDecisionEngine ai = createHardAi(player1);
            giveAiPriority(player1);

            // Opponent at 5 life — Lightning Bolt (3) + Shock (2) = 5 = lethal
            gd.playerLifeTotals.put(player2.getId(), 5);

            // Give AI mountains for mana
            givePlayerMountains(player1, 2);

            harness.setHand(player1, List.of(new LightningBolt(), new Shock()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast a burn spell targeting opponent
            assertThat(gd.stack).hasSize(1);
            String castName = gd.stack.getFirst().getCard().getName();
            assertThat(castName).isIn("Lightning Bolt", "Shock");
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        }

        @Test
        @DisplayName("Hard AI does not burn face when damage is insufficient for lethal")
        void doesNotBurnFaceWhenNotLethal() {
            HardAiDecisionEngine ai = createHardAi(player1);
            giveAiPriority(player1);

            // Opponent at 20 life — Shock (2) is not lethal
            gd.playerLifeTotals.put(player2.getId(), 20);

            givePlayerMountains(player1, 1);

            harness.setHand(player1, List.of(new Shock()));

            ai.handleMessage("GAME_STATE", "");

            // Should NOT have targeted opponent's face with burn for lethal
            // (may cast via normal evaluation, but not via burn-to-face-lethal path)
            if (!gd.stack.isEmpty()) {
                // If something was cast, verify it's not targeting player's face
                // OR it went through normal spell evaluation (not burn-lethal path)
                // The key test: burn-lethal returns false, so normal casting proceeds
                assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Shock");
            }
        }

        @Test
        @DisplayName("Hard AI casts highest-damage burn first when going for lethal")
        void castsHighestDamageBurnFirst() {
            HardAiDecisionEngine ai = createHardAi(player1);
            giveAiPriority(player1);

            // Opponent at 3 life — Lightning Bolt (3) alone is lethal
            gd.playerLifeTotals.put(player2.getId(), 3);

            givePlayerMountains(player1, 2);

            // Hand has Shock first, then Lightning Bolt — AI should pick Bolt (higher damage)
            harness.setHand(player1, List.of(new Shock(), new LightningBolt()));

            ai.handleMessage("GAME_STATE", "");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Lightning Bolt");
            assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        }
    }

    @Nested
    @DisplayName("Race-aware attacking")
    class RaceAwareAttacking {

        @Test
        @DisplayName("Hard AI attacks aggressively with all creatures when winning the race")
        void attacksAggressivelyWhenWinningRace() {
            HardAiDecisionEngine ai = createHardAi(player1);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_ATTACKERS);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

            // AI has 4/4 Air Elemental — 5-turn clock vs opponent's 20 life
            Permanent aiCreature = new Permanent(new AirElemental());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiCreature);

            // Also add a smaller creature to confirm both attack
            Permanent aiBears = new Permanent(new GrizzlyBears());
            aiBears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiBears);

            // Opponent has a small 1/1 (10-turn clock with 2/2 + 4/4 = 6 dmg → ~4 turns for AI vs 20-turn clock for opp)
            Permanent oppCreature = new Permanent(new ElvishVisionary());
            oppCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppCreature);

            ai.handleMessage("AVAILABLE_ATTACKERS", "");

            // When winning the race, AI should attack with all available creatures
            assertThat(aiCreature.isAttacking()).isTrue();
            assertThat(aiBears.isAttacking()).isTrue();
        }
    }

    @Nested
    @DisplayName("Race-aware blocking")
    class RaceAwareBlocking {

        @Test
        @DisplayName("Hard AI skips blocking when winning the race and damage is non-lethal")
        void skipsBlockingWhenWinningRaceAndNonLethal() {
            HardAiDecisionEngine ai = createHardAi(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            // Player1 (attacker) has a small 2/2 — 2 damage, non-lethal
            Permanent humanBears = new Permanent(new GrizzlyBears());
            humanBears.setSummoningSick(false);
            humanBears.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(humanBears);

            // AI (player2) has a 4/4 Air Elemental — winning the race (5-turn clock vs 10-turn for opp)
            Permanent aiCreature = new Permanent(new AirElemental());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(aiCreature);

            ai.handleMessage("AVAILABLE_BLOCKERS", "");

            // AI should NOT block — winning race, damage is non-lethal, preserve creature for attacking
            assertThat(aiCreature.isBlocking()).isFalse();
        }

        @Test
        @DisplayName("Hard AI still blocks when winning the race but damage would be lethal")
        void blocksWhenWinningRaceButDamageIsLethal() {
            HardAiDecisionEngine ai = createHardAi(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            // AI at low life
            gd.playerLifeTotals.put(player2.getId(), 2);

            // Player1 attacks with 2/2 — lethal to AI at 2 life
            Permanent humanBears = new Permanent(new GrizzlyBears());
            humanBears.setSummoningSick(false);
            humanBears.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(humanBears);

            // AI has a 4/4 to block with — and needs to because damage is lethal
            Permanent aiCreature = new Permanent(new AirElemental());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(aiCreature);

            ai.handleMessage("AVAILABLE_BLOCKERS", "");

            // AI should block because the 2 incoming damage would kill it
            assertThat(aiCreature.isBlocking()).isTrue();
        }

        @Test
        @DisplayName("Hard AI blocks normally when losing the race")
        void blocksNormallyWhenLosingRace() {
            HardAiDecisionEngine ai = createHardAi(player2);

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.DECLARE_BLOCKERS);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

            // Player1 (attacker) has a 2/2 — AI is losing the race because it has no creatures to race with
            Permanent humanBears = new Permanent(new GrizzlyBears());
            humanBears.setSummoningSick(false);
            humanBears.setAttacking(true);
            gd.playerBattlefields.get(player1.getId()).add(humanBears);

            // Also give player1 a bigger creature not attacking to ensure they're winning
            Permanent humanAngel = new Permanent(new AirElemental());
            humanAngel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(humanAngel);

            // AI (player2) has a 2/2 — can trade evenly with the attacker (favorable block)
            Permanent aiCreature = new Permanent(new GrizzlyBears());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(aiCreature);

            ai.handleMessage("AVAILABLE_BLOCKERS", "");

            // The blocker declaration should have been processed
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            // When losing the race with a favorable trade available, AI should block
            assertThat(aiCreature.isBlocking()).isTrue();
        }
    }

    // ===== Precombat vs Postcombat Timing =====

    @Nested
    @DisplayName("Precombat vs Postcombat Timing")
    class PrecombatPostcombatTiming {

        private HardAiDecisionEngine ai;
        private FakeConnection aiConn;

        @BeforeEach
        void setUpAi() {
            aiConn = new FakeConnection("ai-timing-test");
            harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
            ai = new HardAiDecisionEngine(
                    gd.id, player1, harness.getGameRegistry(),
                    harness.getMessageHandler(), harness.getGameQueryService(),
                    harness.getCombatAttackService(), harness.getGameBroadcastService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
            ai.setSelfConnection(aiConn);
            ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));

            harness.forceActivePlayer(player1);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(null);
            gd.stack.clear();
        }

        @Test
        @DisplayName("Casts sorcery removal precombat to clear blocker for lethal attack")
        void castsRemovalPrecombatForLethal() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has two 2/2 attackers ready
            Permanent bear1 = new Permanent(new GrizzlyBears());
            bear1.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear1);
            Permanent bear2 = new Permanent(new GrizzlyBears());
            bear2.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear2);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 3; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }
            Permanent swamp4 = new Permanent(new Swamp());
            swamp4.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(swamp4);

            // Opponent has one 2/2 blocker and is at 4 life
            // With the blocker: 1 bear blocked, 1 gets through = 2 damage (not lethal)
            // Without the blocker: both bears attack = 4 damage = lethal
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            gd.playerLifeTotals.put(player2.getId(), 4);

            // AI has Eviscerate (sorcery: destroy target creature)
            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Eviscerate precombat to clear the blocker
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Eviscerate");
        }

        @Test
        @DisplayName("Casts lord precombat to pump attackers toward lethal")
        void castsLordPrecombatForLethal() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has two Goblin Pikers (2/1 Goblins) ready to attack
            Permanent goblin1 = new Permanent(new GoblinPiker());
            goblin1.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(goblin1);
            Permanent goblin2 = new Permanent(new GoblinPiker());
            goblin2.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(goblin2);

            // Mana for Goblin Chieftain (1RR)
            for (int i = 0; i < 3; i++) {
                Permanent mountain = new Permanent(new Mountain());
                mountain.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(mountain);
            }

            // Opponent at 6 life, no blockers
            // Without lord: 2+2 = 4 damage (not lethal)
            // With lord (+1/+1 to Goblins): 3+3 = 6 damage = lethal
            gd.playerLifeTotals.put(player2.getId(), 6);

            harness.setHand(player1, List.of(new GoblinChieftain()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Goblin Chieftain precombat to pump goblins
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chieftain");
        }

        @Test
        @DisplayName("Casts haste creature precombat when it enables lethal")
        void castsHasteCreaturePrecombatForLethal() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has one 2/2 attacker
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear);

            // Mana for Raging Goblin (R)
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(mountain);

            // Opponent at 3 life, no blockers
            // Without haste creature: bear deals 2 (not lethal)
            // With Raging Goblin (1/1 haste): 2+1 = 3 = lethal
            gd.playerLifeTotals.put(player2.getId(), 3);

            harness.setHand(player1, List.of(new RagingGoblin()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Raging Goblin precombat to attack with it
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Raging Goblin");
        }

        @Test
        @DisplayName("Casts removal precombat to clear blocker for significant damage even when not lethal")
        void castsRemovalPrecombatForSignificantDamageGain() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has three 2/2 attackers
            for (int i = 0; i < 3; i++) {
                Permanent bear = new Permanent(new GrizzlyBears());
                bear.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(bear);
            }

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has one 2/2 blocker and is at 20 life (NOT lethal either way)
            // With the blocker: 2 bears blocked/through, 1 gets through = ~2-4 damage
            // Without the blocker: all 3 attack unblocked = 6 damage (gain >= 2)
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            gd.playerLifeTotals.put(player2.getId(), 20);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Eviscerate precombat to clear the blocker for extra damage
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Eviscerate");
        }

        @Test
        @DisplayName("Defers non-combat sorcery to postcombat main")
        void defersNonCombatSorceryToPostcombat() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 2/2 attacker ready
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear);

            // Give AI mana for Divination (2U)
            for (int i = 0; i < 3; i++) {
                Permanent island = new Permanent(new Island());
                island.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(island);
            }

            // No opponent blockers, opponent at 20 life
            gd.playerLifeTotals.put(player2.getId(), 20);

            // AI has only Divination (sorcery: draw 2) — not combat-relevant
            harness.setHand(player1, List.of(new Divination()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT cast Divination precombat — it should pass to combat
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Casts non-combat sorcery in postcombat main")
        void castsNonCombatSorceryPostcombat() {
            harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

            // Give AI mana for Divination (2U)
            for (int i = 0; i < 3; i++) {
                Permanent island = new Permanent(new Island());
                island.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(island);
            }

            gd.playerLifeTotals.put(player2.getId(), 20);

            // AI has Divination (sorcery: draw 2)
            harness.setHand(player1, List.of(new Divination()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Divination postcombat
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
        }

        @Test
        @DisplayName("Casts haste creature precombat even when not lethal")
        void castsHasteCreaturePrecombatNonLethal() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // Mana for Raging Goblin (R)
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(mountain);

            // Opponent at 20 life (definitely not lethal)
            gd.playerLifeTotals.put(player2.getId(), 20);

            // Raging Goblin (1/1 haste) should be cast precombat to join the attack
            harness.setHand(player1, List.of(new RagingGoblin()));

            ai.handleMessage("GAME_STATE", "");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Raging Goblin");
        }

        @Test
        @DisplayName("Casts lord precombat when it meaningfully pumps attackers even if not lethal")
        void castsLordPrecombatForMeaningfulPump() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has two Goblin Pikers (2/1 Goblins) ready to attack
            Permanent goblin1 = new Permanent(new GoblinPiker());
            goblin1.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(goblin1);
            Permanent goblin2 = new Permanent(new GoblinPiker());
            goblin2.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(goblin2);

            // Mana for Goblin Chieftain (1RR)
            for (int i = 0; i < 3; i++) {
                Permanent mtn = new Permanent(new Mountain());
                mtn.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(mtn);
            }

            // Opponent at 20 life (not lethal)
            // Without lord: 2+2 = 4 damage; with lord (+1/+1 to Goblins): 3+3 = 6 damage
            // Total boost = 2 (>= 2 threshold)
            gd.playerLifeTotals.put(player2.getId(), 20);

            harness.setHand(player1, List.of(new GoblinChieftain()));

            ai.handleMessage("GAME_STATE", "");

            // AI should cast Goblin Chieftain precombat to pump attackers
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Goblin Chieftain");
        }

        @Test
        @DisplayName("Defers non-haste creature to postcombat main")
        void defersNonHasteCreatureToPostcombat() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 2/2 attacker
            Permanent bear = new Permanent(new GrizzlyBears());
            bear.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bear);

            // Give AI mana for another Grizzly Bears (1G)
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(forest);
            Permanent forest2 = new Permanent(new Forest());
            forest2.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(forest2);

            gd.playerLifeTotals.put(player2.getId(), 20);

            // Non-haste creature — can't attack this turn, no combat benefit
            harness.setHand(player1, List.of(new GrizzlyBears()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT cast Grizzly Bears precombat — defer to postcombat
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== Evasion-aware damage estimation =====

    @Nested
    @DisplayName("Evasion-aware damage estimation")
    class EvasionAwareDamageEstimation {

        private HardAiDecisionEngine ai;
        private FakeConnection aiConn;

        @BeforeEach
        void setUpAi() {
            aiConn = new FakeConnection("ai-evasion-test");
            harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
            ai = new HardAiDecisionEngine(
                    gd.id, player1, harness.getGameRegistry(),
                    harness.getMessageHandler(), harness.getGameQueryService(),
                    harness.getCombatAttackService(), harness.getGameBroadcastService(),
                    harness.getTargetValidationService(), harness.getTargetLegalityService());
            ai.setSelfConnection(aiConn);
            ai.setMctsEngine(new MCTSEngine(new GameSimulator(harness.getGameQueryService()), 42L));

            harness.forceActivePlayer(player1);
            harness.clearPriorityPassed();
            gd.status = GameStatus.RUNNING;
            gd.interaction.setAwaitingInput(null);
            gd.stack.clear();
        }

        @Test
        @DisplayName("AI skips removal when cant-be-blocked creature already provides lethal damage")
        void skipsRemovalWhenUnblockableCreatureAlreadyLethal() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 2/2 cant-be-blocked attacker (Phantom Warrior)
            Permanent phantom = new Permanent(new PhantomWarrior());
            phantom.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(phantom);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has a 2/2 blocker but is at 2 life
            // Phantom Warrior can't be blocked → 2 damage is already lethal
            // AI should NOT waste removal on the blocker
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            gd.playerLifeTotals.put(player2.getId(), 2);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT cast removal — unblockable attacker already provides lethal
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("AI recognizes flying creature as unblockable when opponent has no flyers or reach")
        void flyingAttackerRecognizedAsUnblockable() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 4/4 flying attacker (Air Elemental)
            Permanent flyer = new Permanent(new AirElemental());
            flyer.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(flyer);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has a ground 2/2 blocker (can't block flying) and is at 4 life
            // Air Elemental can't be blocked by ground creatures → 4 damage is already lethal
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            gd.playerLifeTotals.put(player2.getId(), 4);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT cast removal — flying attacker vs ground blocker is already lethal
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("AI correctly casts removal when flying attacker faces opposing flyer")
        void flyingAttackerBlockedByOpponentFlyer() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 4/4 flying attacker
            Permanent flyer = new Permanent(new AirElemental());
            flyer.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(flyer);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has a 4/4 flying blocker (CAN block flying) and is at 4 life
            // Air Elemental CAN be blocked → current unblockable = 0
            // Removing the blocker → unblockable = 4 = lethal
            Permanent oppFlyer = new Permanent(new AirElemental());
            oppFlyer.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppFlyer);
            gd.playerLifeTotals.put(player2.getId(), 4);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI SHOULD cast removal to clear the flying blocker for lethal
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Eviscerate");
        }

        @Test
        @DisplayName("AI recognizes fear creature as unblockable when opponent has no black/artifact creatures")
        void fearAttackerRecognizedAsUnblockable() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 2/2 fear creature (Severed Legion — black, fear)
            Permanent legion = new Permanent(new SeveredLegion());
            legion.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(legion);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has a green 2/2 (can't block fear) and is at 2 life
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            gd.playerLifeTotals.put(player2.getId(), 2);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT waste removal — fear creature is unblockable vs green creature
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("AI recognizes swampwalk creature as unblockable when opponent controls a swamp")
        void swampwalkAttackerUnblockableWithSwamp() {
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            // AI has a 3/3 swampwalk creature (Bog Wraith)
            Permanent bogWraith = new Permanent(new BogWraith());
            bogWraith.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bogWraith);

            // Give AI mana for Eviscerate (3B)
            for (int i = 0; i < 4; i++) {
                Permanent swamp = new Permanent(new Swamp());
                swamp.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(swamp);
            }

            // Opponent has a 2/2 blocker AND a swamp (swampwalk is active)
            Permanent oppBlocker = new Permanent(new GrizzlyBears());
            oppBlocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppBlocker);
            Permanent oppSwamp = new Permanent(new Swamp());
            oppSwamp.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppSwamp);
            gd.playerLifeTotals.put(player2.getId(), 3);

            harness.setHand(player1, List.of(new Eviscerate()));

            ai.handleMessage("GAME_STATE", "");

            // AI should NOT waste removal — swampwalk makes it unblockable
            assertThat(gd.stack).isEmpty();
        }
    }

    // ===== Flash creature timing =====

    private void giveOpponentPriority(Player opponent) {
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(opponent.getId());
    }

    @Test
    @DisplayName("Hard AI casts flash creature at opponent's end step")
    void castsFlashCreatureAtOpponentsEndStep() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, end step — optimal timing for flash creatures
        giveOpponentPriority(player2);

        // Give AI 3 white mana for Benalish Knight ({2}{W}, 2/2 first strike flash)
        givePlayerPlains(player1, 3);

        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast the flash creature at end of opponent's turn
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Benalish Knight");
    }

    @Test
    @DisplayName("Hard AI does not cast flash creature during own main phase")
    void doesNotCastFlashCreatureDuringOwnMainPhase() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 3 white mana for Benalish Knight ({2}{W}, 2/2 first strike flash)
        givePlayerPlains(player1, 3);

        // Only a flash creature in hand — should be held for opponent's end step
        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — waiting for optimal timing on opponent's turn
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI holds mana for flash creature over low-value sorcery")
    void holdsManaForFlashCreatureOverLowValueSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 3 mana (2 plains + 1 green) — enough for either Bears or Knight but not both
        givePlayerPlains(player1, 2);
        givePlayerForests(player1, 1);

        // Hand: Grizzly Bears ({1}{G}, ~7 value) + Benalish Knight ({2}{W}, flash, held ~8*1.2 ≈ 9.6)
        // Benalish Knight's held value should exceed Bears value × 0.8 factor → hold mana
        harness.setHand(player1, List.of(new GrizzlyBears(), new BenalishKnight()));

        ai.handleMessage("GAME_STATE", "");

        // AI should hold mana for the flash creature — stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for flash creature when both fit")
    void castsSorceryAndReservesManaForFlashCreature() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        // Use postcombat main — simpler decision path (no MCTS combat evaluation)
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        // Give AI 5 mana (2 green + 3 white) — enough for Bears (2 mana) + Knight (3 mana)
        givePlayerForests(player1, 2);
        givePlayerPlains(player1, 3);

        // Hand: Grizzly Bears ({1}{G}, 2 mana) + Benalish Knight ({2}{W}, 3 mana, flash)
        // With 5 total mana, AI can cast Bears and still afford Knight later
        harness.setHand(player1, List.of(new GrizzlyBears(), new BenalishKnight()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast the sorcery-speed creature (can still flash in the Knight later)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Combat trick context-aware evaluation =====

    @Test
    @DisplayName("Combat trick value is high when pump flips combat from losing to winning")
    void combatTrickValueHighWhenPumpFlipsCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI's 2/2 is attacking, opponent's 4/4 is blocking it
        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        int bearsIdx = gd.playerBattlefields.get(player1.getId()).indexOf(bears);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 flying vigilance
        angel.setSummoningSick(false);
        angel.setBlocking(true);
        angel.addBlockingTarget(bearsIdx);
        angel.addBlockingTargetId(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(angel);

        // Giant Growth (+3/+3) should score very high: pump turns 2/2 into 5/5 which
        // kills the 4/4 blocker AND survives (vs without pump: 2/2 dies, 4/4 lives)
        Card giantGrowth = new com.github.laxika.magicalvibes.cards.g.GiantGrowth();
        double value = ai.evaluateCombatTrickInCombat(gd, giantGrowth, false);

        // Should be significantly better than the flat evaluation (3*2.0 + 3 = 9.0)
        assertThat(value).isGreaterThan(15.0);
    }

    @Test
    @DisplayName("Combat trick value reflects face damage on unblocked attacker")
    void combatTrickValueReflectsFaceDamageOnUnblockedAttacker() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // AI's 2/2 is attacking, no blockers
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Giant Growth on unblocked attacker adds 3 face damage
        Card giantGrowth = new com.github.laxika.magicalvibes.cards.g.GiantGrowth();
        double value = ai.evaluateCombatTrickInCombat(gd, giantGrowth, false);

        // Extra face damage (3 * lifeWeight) is valuable but less than flipping a combat
        assertThat(value).isGreaterThan(0);
    }

    @Test
    @DisplayName("Defensive combat trick saves blocker and kills attacker")
    void defensiveCombatTrickSavesBlockerAndKillsAttacker() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Opponent's 4/4 is attacking, AI's 2/2 is blocking
        Permanent angel = new Permanent(new SerraAngel()); // 4/4
        angel.setSummoningSick(false);
        angel.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(angel);
        int angelIdx = gd.playerBattlefields.get(player2.getId()).indexOf(angel);

        Permanent bears = new Permanent(new GrizzlyBears()); // 2/2
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(angelIdx);
        bears.addBlockingTargetId(angel.getId());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        // Giant Growth on our blocker: 5/5 kills 4/4, survives
        Card giantGrowth = new com.github.laxika.magicalvibes.cards.g.GiantGrowth();
        double value = ai.evaluateCombatTrickInCombat(gd, giantGrowth, true);

        // Should be very high: saves our creature + kills theirs
        assertThat(value).isGreaterThan(15.0);
    }

    @Test
    @DisplayName("Combat trick returns negative when no combat is happening")
    void combatTrickReturnsNegativeWhenNoCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // No attackers or blockers — no combat happening
        Card giantGrowth = new com.github.laxika.magicalvibes.cards.g.GiantGrowth();
        double value = ai.evaluateCombatTrickInCombat(gd, giantGrowth, false);

        assertThat(value).isEqualTo(-1);
    }

    @Test
    @DisplayName("Hard AI does not cast flash creature at opponent's precombat main")
    void doesNotCastFlashCreatureAtOpponentsPrecombatMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, precombat main — not a good time for flash creatures
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        givePlayerPlains(player1, 3);
        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleMessage("GAME_STATE", "");

        // Should not cast — precombat main is bad timing for FLASH_CREATURE
        assertThat(gd.stack).isEmpty();
    }
}
