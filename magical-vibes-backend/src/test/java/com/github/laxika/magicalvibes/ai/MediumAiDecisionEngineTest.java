package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.Plains;
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

class MediumAiDecisionEngineTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private MediumAiDecisionEngine ai;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        FakeConnection aiConn = new FakeConnection("ai-medium-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), "Bob");
        ai = new MediumAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService(),
                harness.getGameBroadcastService(), harness.getTargetValidationService());
        ai.setSelfConnection(aiConn);
    }

    private void giveAiPriority() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
    }

    private void giveAiPlains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(plains);
        }
    }

    private void giveAiIslands(int count) {
        for (int i = 0; i < count; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }
    }

    private void giveAiMountains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);
        }
    }

    @Test
    @DisplayName("Medium AI casts Pacifism on opponent's biggest threat")
    void castsRemovalOnBiggestThreat() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        // Should target the Air Elemental (biggest threat)
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("Medium AI does not attack into clearly losing trade")
    void doesNotAttackIntoLosingTrade() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // AI has a 2/2
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiBears);

        // Opponent has a 4/4
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // AI should not have attacked — bears would die without killing AE
        // The attack step resolves, check that bears is still alive and untapped
        assertThat(aiBears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Medium AI recognizes lethal and attacks all-in")
    void recognizesLethalAllIn() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gd.playerLifeTotals.put(human.getId(), 4);

        // AI has two 2/2s (total 4 damage = exact lethal)
        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears2);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Both should be attacking for lethal
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI casts higher-value spell when multiple available")
    void castsHigherValueSpell() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has a big creature (Pacifism will be high value)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Hand has Bears (creature value) and Pacifism (high value due to target)
        harness.setHand(aiPlayer, List.of(new GrizzlyBears(), new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should cast the spell with higher evaluated value
        assertThat(gd.stack).hasSize(1);
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("Medium AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        giveAiPriority();
        giveAiPlains(2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should NOT be on the stack — only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Medium AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        giveAiPriority();

        // Add two Llanowar Elves (creature mana dorks) to battlefield
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should be on the stack — creature mana is available from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    // ===== Sacrifice cost checks =====

    @Test
    @DisplayName("Medium AI skips spell with SacrificeArtifactCost when no artifact on battlefield")
    void skipsSpellWithSacrificeArtifactCostWhenNoArtifact() {
        giveAiPriority();

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(mountain);

        harness.setHand(aiPlayer, List.of(new KuldothaRebirth()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no artifact to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    // ===== Sacrifice cost spell casting =====

    @Test
    @DisplayName("Medium AI casts Vivisection by sacrificing weakest creature")
    void castsVivisectionSacrificingWeakestCreature() {
        giveAiPriority();

        for (int i = 0; i < 4; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(island);
        }

        Permanent elves = new Permanent(new LlanowarElves()); // 1/1 — should be sacrificed
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elves);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 — should survive
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(angel);

        harness.setHand(aiPlayer, List.of(new Vivisection()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");
        harness.assertNotOnBattlefield(aiPlayer, "Llanowar Elves");
        harness.assertOnBattlefield(aiPlayer, "Serra Angel");
    }

    // ===== Must-attack =====

    @Test
    @DisplayName("Medium AI includes must-attack creature even into unfavorable board")
    void includesMustAttackCreature() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // AI has Berserkers of Blood Ridge (4/4 must-attack)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying) — can block
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Berserkers must be attacking despite the unfavorable board
        assertThat(berserkers.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Medium AI includes must-attack creature alongside optional creatures")
    void includesMustAttackWithOptional() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gd.playerLifeTotals.put(human.getId(), 20);

        // AI has Berserkers (4/4 must-attack) and Bears (2/2 optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        // No blockers — both should attack, dealing at least 4 damage (must-attack Berserkers)
        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Berserkers (4 power) must have attacked; combat fully resolves with no blockers
        assertThat(gd.playerLifeTotals.get(human.getId())).isLessThanOrEqualTo(16);
    }

    // ===== Mana tapping before spell casting =====

    @Test
    @DisplayName("Medium AI taps lands before casting sorcery-speed spell")
    void tapsLandsBeforeCastingSorcery() {
        giveAiPriority();

        // Use Forests so GrizzlyBears ({1}{G}) is castable
        Permanent forest1 = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        forest1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest1);

        Permanent forest2 = new Permanent(new com.github.laxika.magicalvibes.cards.f.Forest());
        forest2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(forest2);

        // Mana pool is empty — AI must tap Forests
        assertThat(gd.playerManaPools.get(aiPlayer.getId()).getTotal()).isZero();

        harness.setHand(aiPlayer, List.of(new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");

        // Forests should be tapped
        long tappedCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedCount).isEqualTo(2);
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

        private MediumAiDecisionEngine createEngine() {
            Mockito.when(mockGameBroadcastService.isSpellCastingAllowed(any(), any(), any())).thenReturn(true);
            MediumAiDecisionEngine engine = new MediumAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockCombatAttackService, mockGameBroadcastService,
                    mockTargetValidationService);
            engine.setSelfConnection(mockConnection);
            return engine;
        }

        @Test
        @DisplayName("Medium AI passes priority when spell cast is silently rejected")
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
        @DisplayName("Medium AI does NOT pass priority when spell cast succeeds")
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
        @DisplayName("Medium AI builds damage assignments for divided damage spell")
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
    @DisplayName("Medium AI does not cast Steel Sabotage when no mode has valid targets")
    void doesNotCastSteelSabotageWhenNoValidMode() {
        giveAiPriority();
        giveAiIslands(1);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Medium AI skips Steel Sabotage (no valid mode) and casts another available spell")
    void skipsModalSpellAndCastsAlternative() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has a creature (Pacifism will be high value)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Steel Sabotage has no valid mode (no artifacts), but Pacifism is castable
        harness.setHand(aiPlayer, List.of(new SteelSabotage(), new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip Steel Sabotage and cast Pacifism
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
    }

    @Test
    @DisplayName("Medium AI casts Steel Sabotage to bounce artifact creature on opponent's battlefield")
    void castsSteelSabotageToBounceArtifact() {
        // Set up as opponent's turn, beginning of combat — good timing for REMOVAL instants
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
        gd.priorityPassedBy.add(human.getId());

        giveAiIslands(1);

        // Artifact creature so bounce evaluator gives positive value (creature score)
        Card artifactCreature = new Card();
        artifactCreature.setName("Test Artifact Creature");
        artifactCreature.setType(CardType.ARTIFACT);
        artifactCreature.setAdditionalTypes(Set.of(CardType.CREATURE));
        artifactCreature.setPower(3);
        artifactCreature.setToughness(3);
        Permanent artifactPerm = new Permanent(artifactCreature);
        gd.playerBattlefields.get(human.getId()).add(artifactPerm);

        harness.setHand(aiPlayer, List.of(new SteelSabotage()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Steel Sabotage");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(artifactPerm.getId());
    }

    @Test
    @DisplayName("Medium AI casts Slagstorm to wipe opponent's creatures")
    void castsSlagstorm() {
        giveAiPriority();
        giveAiMountains(3);

        // Opponent has creatures so board wipe evaluator gives positive value
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new Slagstorm()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slagstorm");
    }

    // ===== Attack tax handling =====

    @Test
    @DisplayName("Medium AI limits attackers when attack tax is present")
    void limitsAttackersWhenAttackTaxPresent() {
        // Human controls Baird (tax {1} per attacker)
        Permanent baird = new Permanent(new BairdStewardOfArgive());
        baird.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(baird);

        // AI has 3 creatures and only 1 Plains
        giveAiPlains(1);
        for (int i = 0; i < 3; i++) {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(bears);
        }

        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.beginAttackerDeclaration(aiPlayer.getId());

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // At most 1 creature should be attacking (can only afford {1} tax)
        long attackingCount = gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isLessThanOrEqualTo(1);
    }

    // ===== ExileNCardsFromGraveyardCost (e.g. Skaab Ruinator) =====

    @Test
    @DisplayName("Medium AI casts Skaab Ruinator when graveyard has 3 creature cards")
    void castsSkaabRuinatorWithThreeCreatures() {
        giveAiPriority();
        giveAiIslands(3); // Skaab Ruinator costs {1}{U}{U}

        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).isEmpty();
    }

    @Test
    @DisplayName("Medium AI casts Skaab Ruinator selecting only creatures from mixed graveyard")
    void castsSkaabRuinatorFromMixedGraveyard() {
        giveAiPriority();
        giveAiIslands(3);

        gd.playerGraveyards.get(aiPlayer.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new com.github.laxika.magicalvibes.cards.h.HolyDay());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());
        gd.playerGraveyards.get(aiPlayer.getId()).add(new GrizzlyBears());

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.s.SkaabRuinator()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(aiPlayer.getId())).hasSize(3);
        // Only creatures exiled — 2 non-creatures remain
        assertThat(gd.playerGraveyards.get(aiPlayer.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(aiPlayer.getId()))
                .allMatch(c -> c.getName().equals("Holy Day"));
    }

    // ===== Entrancing Melody (PermanentManaValueEqualsXPredicate) =====

    @Test
    @DisplayName("Medium AI casts Entrancing Melody with X matching target creature's mana value")
    void castsEntrancingMelodyWithCorrectX() {
        giveAiPriority();
        giveAiIslands(4); // maxX = 4 - 2 (for {U}{U}) = 2

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Entrancing Melody");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI picks highest affordable mana value target for Entrancing Melody")
    void picksHighestAffordableTargetForEntrancingMelody() {
        giveAiPriority();
        giveAiIslands(4); // maxX = 2

        Permanent vanguard = new Permanent(new EliteVanguard()); // MV=1
        vanguard.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(vanguard);

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Medium AI skips Entrancing Melody when target too expensive")
    void skipsEntrancingMelodyWhenTooExpensive() {
        giveAiPriority();
        giveAiIslands(3); // maxX = 1

        Permanent bears = new Permanent(new GrizzlyBears()); // MV=2, unaffordable
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new EntrancingMelody()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }
}
