package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AwakenerDruid;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AiDecisionEngineTest {

    private GameTestHarness harness;
    private Player human;
    private Player aiPlayer;
    private GameData gd;
    private EasyAiDecisionEngine ai;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        human = harness.getPlayer1();
        aiPlayer = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();

        FakeConnection aiConn = new FakeConnection("ai-test");
        harness.getSessionManager().registerPlayer(aiConn, aiPlayer.getId(), "Bob");
        ai = new EasyAiDecisionEngine(gd.id, aiPlayer, harness.getGameRegistry(),
                harness.getMessageHandler(), harness.getGameQueryService(), harness.getCombatAttackService());
        ai.setSelfConnection(aiConn);
    }

    /**
     * Sets up the game so the AI (player2) has priority in precombat main with an empty stack.
     */
    private void giveAiPriority() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(null);
        gd.stack.clear();
    }

    /**
     * Adds the given number of untapped Plains to the AI's battlefield.
     */
    private void giveAiPlains(int count) {
        for (int i = 0; i < count; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(plains);
        }
    }

    // ===== Detrimental aura targeting =====

    @Test
    @DisplayName("AI casts Pacifism on opponent's highest-power creature")
    void castsPacifismOnHighestPowerCreature() {
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
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(airElemental.getId());
    }

    @Test
    @DisplayName("AI does not cast second Pacifism on already-pacified creature")
    void doesNotDoublePacify() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already has Pacifism attached
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast on the Grizzly Bears instead
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Pacifism");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("AI does not cast Pacifism when all opponent creatures already have one")
    void doesNotCastWhenAllCreaturesPacified() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        // Bears already has Pacifism
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid targets, so it passes priority instead
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Pacifism when opponent has mix of pacified and unpacified creatures")
    void castsOnUnpacifiedAmongMixed() {
        giveAiPriority();
        giveAiPlains(2);

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears2);

        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(airElemental);

        // Air Elemental already pacified
        Permanent existingPacifism = new Permanent(new Pacifism());
        existingPacifism.setAttachedTo(airElemental.getId());
        gd.playerBattlefields.get(aiPlayer.getId()).add(existingPacifism);

        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        // Should target one of the unpacified bears (both 2/2, either is valid)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId())
                .isIn(bears1.getId(), bears2.getId());
    }

    // ===== Beneficial aura targeting =====

    @Test
    @DisplayName("AI casts beneficial aura on own creature")
    void castsBeneficialAuraOnOwnCreature() {
        giveAiPriority();

        // Holy Strength costs {W}
        Permanent plains = new Permanent(new Plains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(plains);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new HolyStrength()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Holy Strength");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    // ===== No target available =====

    @Test
    @DisplayName("AI does not cast Pacifism when opponent has no creatures")
    void doesNotCastWithoutOpponentCreatures() {
        giveAiPriority();
        giveAiPlains(2);

        // Opponent has no creatures
        harness.setHand(aiPlayer, List.of(new Pacifism()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).isEmpty();
    }

    // ===== Target filter fallback =====

    @Test
    @DisplayName("AI casts Awakener Druid targeting own Forest")
    void castsAwakenerDruidTargetingForest() {
        giveAiPriority();

        // Awakener Druid costs {2}{G} — give AI 3 Forests (one will be the target)
        for (int i = 0; i < 3; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }

        harness.setHand(aiPlayer, List.of(new AwakenerDruid()));

        ai.handleMessage("GAME_STATE", "");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Awakener Druid");
        // Target must be one of the AI's Forests
        UUID targetId = gd.stack.getFirst().getTargetId();
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst()
                .orElseThrow()
                .getCard().getName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("AI does not cast Awakener Druid when no Forests on battlefield")
    void doesNotCastAwakenerDruidWithoutForests() {
        giveAiPriority();

        // Give AI enough mana for {2}{G} via Swamps + floating green, but no Forest permanents
        for (int i = 0; i < 2; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(swamp);
        }
        harness.addMana(aiPlayer, ManaColor.GREEN, 1);

        harness.setHand(aiPlayer, List.of(new AwakenerDruid()));

        ai.handleMessage("GAME_STATE", "");

        // No valid Forest target — AI should pass priority instead
        assertThat(gd.stack).isEmpty();
    }

    // ===== Creature mana restriction =====

    @Test
    @DisplayName("AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        giveAiPriority();
        giveAiPlains(2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should NOT be on the stack — only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        giveAiPriority();

        // Add two Llanowar Elves (creature mana dorks)
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(elf2);

        harness.setHand(aiPlayer, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleMessage("GAME_STATE", "");

        // Myr Superion should be on the stack — creature mana from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    @Test
    @DisplayName("AI skips Myr Superion but casts normal spell when only land mana available")
    void skipsMyrSuperionButCastsNormalSpell() {
        giveAiPriority();

        // Use Forests so GrizzlyBears ({1}{G}) is castable
        for (int i = 0; i < 2; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(aiPlayer.getId()).add(forest);
        }

        harness.setHand(aiPlayer, List.of(
                new com.github.laxika.magicalvibes.cards.m.MyrSuperion(),
                new GrizzlyBears()));

        ai.handleMessage("GAME_STATE", "");

        // Should skip Myr Superion and cast the GrizzlyBears instead
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    // ===== Blocker declaration =====

    /**
     * Sets up the game so the human is the active player attacking, and the AI is defending.
     */
    private void setupBlockerPhase() {
        harness.forceActivePlayer(human);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }

    @Test
    @DisplayName("AI does not attempt to block an unblockable creature")
    void doesNotBlockUnblockableCreature() {
        setupBlockerPhase();

        // Human attacks with Phantom Warrior (unblockable)
        Permanent phantomWarrior = new Permanent(new PhantomWarrior());
        phantomWarrior.setSummoningSick(false);
        phantomWarrior.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(phantomWarrior);

        // AI has a creature that could theoretically block
        Permanent aiBears = new Permanent(new GrizzlyBears());
        aiBears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiBears);

        // Should not throw — AI skips the unblockable attacker
        ai.handleMessage("AVAILABLE_BLOCKERS", "");

        // Bears should not be tapped (not assigned as blocker)
        assertThat(aiBears.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("AI blocks normal attacker but skips unblockable attacker")
    void blocksNormalButSkipsUnblockable() {
        setupBlockerPhase();

        // Human attacks with Phantom Warrior (unblockable) and Grizzly Bears (blockable)
        Permanent phantomWarrior = new Permanent(new PhantomWarrior());
        phantomWarrior.setSummoningSick(false);
        phantomWarrior.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(phantomWarrior);

        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(humanBears);

        // AI has Air Elemental — big enough to favorably block Grizzly Bears (4/4 vs 2/2)
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiElemental);

        // Set life low enough that lethal incoming triggers chump-block logic too
        gd.playerLifeTotals.put(aiPlayer.getId(), 3);

        ai.handleMessage("AVAILABLE_BLOCKERS", "");
        harness.passBothPriorities();

        // Combat fully resolves — assert on outcomes:
        // Phantom Warrior was unblocked, so only its 2 damage got through (3 - 2 = 1)
        assertThat(gd.playerLifeTotals.get(aiPlayer.getId())).isEqualTo(1);
        // Grizzly Bears was blocked by Air Elemental and died
        assertThat(gd.playerBattlefields.get(human.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Air Elemental survived (4/4 vs 2/2)
        assertThat(gd.playerBattlefields.get(aiPlayer.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Air Elemental"));
    }

    // ===== Must-attack =====

    private void setupAttackerPhase() {
        harness.forceActivePlayer(aiPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
    }

    @Test
    @DisplayName("Easy AI includes must-attack creature in attack declaration")
    void includesMustAttackCreature() {
        setupAttackerPhase();

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
    @DisplayName("Easy AI includes must-attack creature alongside optional creatures")
    void includesMustAttackWithOptional() {
        setupAttackerPhase();
        gd.playerLifeTotals.put(human.getId(), 20);

        // AI has Berserkers (4/4 must-attack) and Bears (2/2 optional)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(berserkers);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(bears);

        ai.handleMessage("AVAILABLE_ATTACKERS", "");

        // Berserkers (4 power) must have attacked; combat fully resolves with no blockers
        assertThat(gd.playerLifeTotals.get(human.getId())).isLessThanOrEqualTo(16);
    }

    // ===== Blocker fallback =====

    @Test
    @DisplayName("AI does not get stuck when blocker declaration is rejected")
    void blockerFallbackOnInvalidDeclaration() {
        setupBlockerPhase();

        // Human attacks with Grizzly Bears
        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(human.getId()).add(humanBears);

        // AI has Air Elemental to block with
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(aiPlayer.getId()).add(aiElemental);

        ai.handleMessage("AVAILABLE_BLOCKERS", "");

        // The blocker declaration should have been accepted (no stuck state)
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    // ===== ETB destroy targeting =====

    @Test
    @DisplayName("AI casts Aven Cloudchaser targeting opponent's enchantment, not a creature")
    void castsAvenCloudchaserTargetingEnchantment() {
        giveAiPriority();
        giveAiPlains(4);

        // Opponent has a creature and an enchantment
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        Permanent chorus = new Permanent(new AngelicChorus());
        gd.playerBattlefields.get(human.getId()).add(chorus);

        harness.setHand(aiPlayer, List.of(new AvenCloudchaser()));

        ai.handleMessage("GAME_STATE", "");

        // AI should cast Aven Cloudchaser targeting the enchantment, not the creature
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Aven Cloudchaser");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(chorus.getId());
    }

    @Test
    @DisplayName("AI does not cast Aven Cloudchaser when no enchantments on battlefield")
    void doesNotCastAvenCloudchaserWithoutEnchantments() {
        giveAiPriority();
        giveAiPlains(4);

        // Opponent has only creatures, no enchantments
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(human.getId()).add(bears);

        harness.setHand(aiPlayer, List.of(new AvenCloudchaser()));

        ai.handleMessage("GAME_STATE", "");

        // AI should not cast — no valid enchantment targets
        assertThat(gd.stack).isEmpty();
    }

    // ===== tryPlayLand silent failure recovery =====

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("tryPlayLand silent failure recovery")
    class TryPlayLandSilentFailureRecovery {

        @Mock private MessageHandler mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private Connection mockConnection;

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

        private EasyAiDecisionEngine createEngine() {
            EasyAiDecisionEngine engine = new EasyAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockCombatAttackService);
            engine.setSelfConnection(mockConnection);
            return engine;
        }

        @Test
        @DisplayName("AI passes priority when land play is silently rejected")
        void passesPriorityWhenLandPlaySilentlyRejected() throws Exception {
            Card land = new Card();
            land.setName("Test Plains");
            land.setType(CardType.LAND);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(land);

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler).handlePassPriority(any(), any());
        }

        @Test
        @DisplayName("AI does NOT pass priority when land play succeeds")
        void doesNotPassPriorityWhenLandPlaySucceeds() throws Exception {
            Card land = new Card();
            land.setName("Test Plains");
            land.setType(CardType.LAND);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(land);

            Mockito.doAnswer(inv -> {
                mockGd.playerHands.get(mockAiPlayer.getId()).removeFirst();
                return null;
            }).when(mockMessageHandler).handlePlayCard(any(), any());

            createEngine().handleMessage("GAME_STATE", "");

            verify(mockMessageHandler).handlePlayCard(any(), any());
            verify(mockMessageHandler, never()).handlePassPriority(any(), any());
        }
    }
}


