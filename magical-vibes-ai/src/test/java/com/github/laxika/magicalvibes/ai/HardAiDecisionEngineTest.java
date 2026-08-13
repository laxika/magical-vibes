package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.testutil.TestCards;
import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.HeadlessSimulationContext;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.t.TroveOfTemptation;
import com.github.laxika.magicalvibes.cards.t.TragedyFeaster;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AbandonHope;
import com.github.laxika.magicalvibes.cards.a.AjanisResponse;
import com.github.laxika.magicalvibes.cards.b.BairdStewardOfArgive;
import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.b.Blight;
import com.github.laxika.magicalvibes.cards.b.BlindingBeam;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.b.BerserkersOfBloodRidge;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.CrypticCommand;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EkunduCyclops;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.e.EntrancingMelody;
import com.github.laxika.magicalvibes.cards.e.Errantry;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.cards.f.FitOfRage;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinChieftain;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mindslaver;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.b.BogardanFirefiend;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.cards.i.IronStar;
import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.k.KillerBees;
import com.github.laxika.magicalvibes.cards.v.VigilForTheLost;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.Negate;
import com.github.laxika.magicalvibes.cards.n.Nekrataal;
import com.github.laxika.magicalvibes.cards.o.OrcishConscripts;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
import com.github.laxika.magicalvibes.cards.s.SmiteTheMonstrous;
import com.github.laxika.magicalvibes.cards.s.SteelSabotage;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unbury;
import com.github.laxika.magicalvibes.cards.v.Vivisection;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameActionAvailabilityService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.block.BlockLegalityService;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.CounterType;

@Tag("scryfall")
class HardAiDecisionEngineTest extends HardAiDecisionEngineTestSupport {

    private void givePlayerSwamps(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(swamp);
        }
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

        GameSimulator simulator = HeadlessSimulationContext.getSimulator();
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
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));

        GameSimulator simulator = HeadlessSimulationContext.getSimulator();
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 200);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.DeclareAttackers.class);
        assertThat(elapsed).isLessThan(3000);
    }

    @Test
    @DisplayName("MCTS search completes within time budget for blocker declaration")
    void mctsSearchCompletesForBlockers() {
        // Add blockers for player1
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerraAngel());
        gd.playerBattlefields.get(player1.getId()).forEach(p -> p.setSummoningSick(false));

        // Add attacking creatures for player2
        harness.addToBattlefield(player2, new EliteVanguard());
        harness.addToBattlefield(player2, new BerserkersOfBloodRidge());
        gd.playerBattlefields.get(player2.getId()).forEach(p -> {
            p.setSummoningSick(false);
            p.setAttacking(true);
        });

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player2);
        gd.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(player1.getId()));

        GameSimulator simulator = HeadlessSimulationContext.getSimulator();
        MCTSEngine engine = new MCTSEngine(simulator);

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 200);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.DeclareBlockers.class);
        assertThat(elapsed).isLessThan(3000);
    }

    @Test
    @DisplayName("Hard AI does not submit a creature barred by Island Sanctuary")
    void doesNotSubmitCreatureBarredByIslandSanctuary() {
        HardAiDecisionEngine ai = createHardAi(player1);
        harness.addToBattlefield(player2, new IslandSanctuary());
        gd.turnNumber = 2;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
        assertThat(attacker.isAttacking()).isFalse();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("HardAiDecisionEngine constructor initializes without errors")
    void hardEngineConstructorWorks() {
        HardAiDecisionEngine engine = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        assertThat(engine).isNotNull();
    }

    @Test
    @DisplayName("Hard AI avoids tapping a land enchanted by Blight when other lands can pay")
    void avoidsTappingLandEnchantedByBlight() {
        FakeConnection aiConn = new FakeConnection("ai-hard-blight-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(),
                harness.getCastingPermissionService(), harness.getTargetValidationService(),
                harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        Permanent blightedPlains = new Permanent(new Plains());
        Permanent forest = new Permanent(new Forest());
        Permanent island = new Permanent(new Island());
        blightedPlains.setSummoningSick(false);
        forest.setSummoningSick(false);
        island.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blightedPlains);
        gd.playerBattlefields.get(player1.getId()).add(forest);
        gd.playerBattlefields.get(player1.getId()).add(island);

        Permanent blight = new Permanent(new Blight());
        blight.setAttachedTo(blightedPlains.getId());
        gd.playerBattlefields.get(player2.getId()).add(blight);

        harness.setHand(player1, List.of(new GrizzlyBears()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(blightedPlains.isTapped()).isFalse();
        assertThat(forest.isTapped()).isTrue();
        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hard AI skips spell with a sacrifice-an-artifact cost when no artifact on battlefield")
    void skipsSpellWithSacrificeArtifactCostWhenNoArtifact() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of(new KuldothaRebirth()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should not cast A?€�t no artifact to sacrifice
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Vivisection by sacrificing weakest creature")
    void castsVivisectionSacrificingWeakestCreature() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        // Use postcombat A?€�t Vivisection is non-combat so Hard AI defers it past combat
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        for (int i = 0; i < 4; i++) {
            Permanent island = new Permanent(new Island());
            island.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island);
        }

        Permanent elves = new Permanent(new LlanowarElves()); // 1/1 A?€�t should be sacrificed
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);

        Permanent angel = new Permanent(new SerraAngel()); // 4/4 A?€�t should survive
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.setHand(player1, List.of(new Vivisection()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vivisection");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Hard AI does not cast Myr Superion with only land mana")
    void doesNotCastMyrSuperionWithLandMana() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // Give AI 2 Plains (land mana only)
        for (int i = 0; i < 2; i++) {
            Permanent plains = new Permanent(new com.github.laxika.magicalvibes.cards.p.Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(plains);
        }

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Myr Superion should NOT be on the stack A?€�t only land mana is available
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts Myr Superion when creature mana dorks are available")
    void castsMyrSuperionWithCreatureMana() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        // Use postcombat A?€�t non-haste creature deferred past combat by Hard AI
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // Add two Llanowar Elves (creature mana dorks)
        Permanent elf1 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf1);

        Permanent elf2 = new Permanent(new com.github.laxika.magicalvibes.cards.l.LlanowarElves());
        elf2.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elf2);

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.m.MyrSuperion()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Myr Superion should be on the stack A?€�t creature mana is available from elves
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Myr Superion");
    }

    @Test
    @DisplayName("Hard AI supplies untapped creatures for convoke")
    void castsConvokeSpellWithUntappedCreatures() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        givePlayerIslands(player1, 2);
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstCreature.setSummoningSick(false);
        secondCreature.setSummoningSick(false);

        Card convokeSpell = new Card();
        convokeSpell.setName("Convoke Test Creature");
        convokeSpell.setType(CardType.CREATURE);
        convokeSpell.setManaCost("{3}{U}");
        convokeSpell.setPower(4);
        convokeSpell.setToughness(4);
        convokeSpell.setKeywords(EnumSet.of(Keyword.CONVOKE));
        harness.setHand(player1, List.of(convokeSpell));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(convokeSpell);
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Hard AI does not cast Nekrataal when the only target for its ETB kill is its own Serra Angel")
    void doesNotCastNekrataalWhenOnlyOwnCreatureIsTargetable() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        // Postcombat with a single castable spell exercises the evaluator path
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        for (int i = 0; i < 4; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(swamp);
        }

        // AI's only creature is a Serra Angel; the opponent's board is empty, so the
        // mandatory ETB kill could only hit the angel A?€�t far worse than the 2/1 body gained.
        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.setHand(player1, List.of(new Nekrataal()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Hard AI still casts Nekrataal when the opponent has a legal creature to kill")
    void castsNekrataalWhenOpponentHasLegalTarget() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        for (int i = 0; i < 4; i++) {
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(swamp);
        }

        Permanent angel = new Permanent(new SerraAngel());
        angel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        // Opponent has a legal (white, nonartifact) creature A?€�t Nekrataal is a clean two-for-one
        Permanent oppAngel = new Permanent(new SerraAngel());
        oppAngel.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(oppAngel);

        harness.setHand(player1, List.of(new Nekrataal()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nekrataal");
    }

    /**
     * Hands player2's seat control of player1's turn through a real Mindslaver activation and
     * stops in that turn's declare-attackers step, returning the controlled player's must-attack
     * creature.
     *
     * <p>The engine addresses the attacker decision to the controller while the interaction stays
     * owned by the controlled player, so the AI has to declare from a battlefield that is not its
     * own. The controlled player is deliberately given more permanents than the AI seat and the
     * must-attack creature is put last, so an engine reading its own battlefield can only produce
     * indices that name something else — or nothing at all.
     */
    private Permanent mindslavedAttackerDeclaration() {
        Set<TurnStep> mainPhaseStops = Set.of(TurnStep.PRECOMBAT_MAIN, TurnStep.POSTCOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), ConcurrentHashMap.newKeySet());
        gd.playerAutoStopSteps.get(player1.getId()).addAll(mainPhaseStops);
        gd.playerAutoStopSteps.put(player2.getId(), ConcurrentHashMap.newKeySet());
        gd.playerAutoStopSteps.get(player2.getId()).addAll(mainPhaseStops);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new Mindslaver());
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        // Roll into the controlled player's turn, where the control actually takes effect.
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.mindControlledPlayerId).isEqualTo(player1.getId());
        assertThat(gd.mindControllerPlayerId).isEqualTo(player2.getId());

        Permanent ownBears = new Permanent(new GrizzlyBears());
        ownBears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ownBears);

        for (int i = 0; i < 3; i++) {
            Permanent forest = new Permanent(new Forest());
            forest.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(forest);
        }
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.stack.clear();
        harness.beginAttackerDeclarationInput();
        return berserkers;
    }

    @Test
    @DisplayName("Hard AI attacks from the Mindslavered player's battlefield, not its own")
    void declaresAttackersForMindslaveredPlayer() {
        Permanent berserkers = mindslavedAttackerDeclaration();

        FakeConnection aiConn = new FakeConnection("ai-hard-mindslaver-test");
        harness.getSessionManager().registerPlayer(aiConn, player2.getId(), "Bob");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player2, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        assertThat(berserkers.isAttacking()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(Permanent::isAttacking);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Hard AI includes must-attack creature in attack declaration")
    void includesMustAttackCreature() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginAttackerDeclarationInput();

        // AI has Berserkers of Blood Ridge (4/4 must-attack)
        Permanent berserkers = new Permanent(new BerserkersOfBloodRidge());
        berserkers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(berserkers);

        // Opponent has Air Elemental (4/4 flying)
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(airElemental);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        assertThat(berserkers.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Hard AI includes an attack-if-another-attacks creature")
    void includesConditionalAttackRequirement() {
        gd.playerLifeTotals.put(player2.getId(), 5);
        Permanent cyclops = harness.addToBattlefieldAndReturn(player1, new EkunduCyclops());
        cyclops.setSummoningSick(false);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        HardAiDecisionEngine ai = createHardAi(player1);

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(cyclops.isAttacking()).isTrue();
        assertThat(bears.isAttacking()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Hard AI does not declare Orcish Conscripts without enough other attackers")
    void doesNotDeclareOrcishConscriptsWithoutEnoughOtherAttackers() {
        gd.playerLifeTotals.put(player2.getId(), 4);
        Permanent conscripts = harness.addToBattlefieldAndReturn(player1, new OrcishConscripts());
        conscripts.setSummoningSick(false);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ally.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        HardAiDecisionEngine ai = createHardAi(player1);

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }

        assertThat(conscripts.isAttackedThisTurn()).isFalse();
        assertThat(ally.isAttackedThisTurn()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Hard AI removes a creature that can only attack alone from a larger group")
    void removesCanOnlyAttackAloneCreature() {
        Permanent restricted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        restricted.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        aura.setAttachedTo(restricted.getId());
        Permanent unrestricted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        unrestricted.setSummoningSick(false);

        List<Integer> result = createHardAi(player1).prepareAttackersForTax(gd, List.of(0, 2));

        assertThat(result).containsExactly(2);
    }

    @Test
    @DisplayName("Hard AI keeps one creature when every selected creature can only attack alone")
    void keepsOneWhenAllCanOnlyAttackAlone() {
        Permanent firstRestricted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        firstRestricted.setSummoningSick(false);
        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        firstAura.setAttachedTo(firstRestricted.getId());
        Permanent secondRestricted = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        secondRestricted.setSummoningSick(false);
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new Errantry());
        secondAura.setAttachedTo(secondRestricted.getId());

        List<Integer> result = createHardAi(player1).prepareAttackersForTax(gd, List.of(0, 2));

        assertThat(result).containsExactly(0);
    }

    @Test
    @DisplayName("Hard AI ignores a stale attacker-declaration event")
    void ignoresStaleAttackerDeclarationEvent() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        HardAiDecisionEngine ai = createHardAi(player1);

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);
        TurnStep stepAfterDeclaration = gd.currentStep;
        int lifeAfterDeclaration = gd.getLife(player2.getId());

        FuzzLogWatcher watcher = FuzzLogWatcher.install();
        try {
            ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

            assertThat(watcher.drainFailures()).isEmpty();
        } finally {
            watcher.uninstall();
        }
        assertThat(lifeAfterDeclaration).isEqualTo(18);
        assertThat(gd.currentStep).isEqualTo(stepAfterDeclaration);
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterDeclaration);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.AttackerDeclaration.class)).isNull();
    }

    @Test
    @DisplayName("Hard AI ignores a blocker-declaration event for another player")
    void ignoresBlockerDeclarationForAnotherPlayer() {
        gd.interaction.beginInteraction(new PendingInteraction.BlockerDeclaration(player1.getId()));
        AiGameActions actions = Mockito.mock(AiGameActions.class);
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player2, harness.getGameRegistry(), actions,
                harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(),
                harness.getTargetValidationService(), harness.getTargetLegalityService());

        ai.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        verify(actions, never()).handleDeclareBlockers(any());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class).decidingPlayerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Hard AI blocker declaration does not leave game stuck")
    void blockerDeclarationDoesNotStick() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player2.getId(), "Bob");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player2, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        harness.beginBlockerDeclarationInput();

        // Player1 attacks with Grizzly Bears
        Permanent humanBears = new Permanent(new GrizzlyBears());
        humanBears.setSummoningSick(false);
        humanBears.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(humanBears);

        // AI has Air Elemental to block with
        Permanent aiElemental = new Permanent(new AirElemental());
        aiElemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(aiElemental);

        ai.handleEvent(AiDecisionKind.BLOCKER_DECLARATION);

        // The blocker declaration should have been accepted
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("tryCastSpell silent failure recovery")
    class TryCastSpellSilentFailureRecovery {

        @Mock private AiGameActions mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private BlockLegalityService mockBlockLegalityService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private GameActionAvailabilityService mockGameActionAvailabilityService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService mockCastingCostService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingPermissionService mockCastingPermissionService;
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
            AiTestPlayabilityStub.install(mockGameActionAvailabilityService, mockCastingCostService, mockGameQueryService);
            HardAiDecisionEngine engine = new HardAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockBlockLegalityService, mockCombatAttackService, mockGameActionAvailabilityService,
                    mockCastingCostService, mockCastingPermissionService,
                    mockTargetValidationService,
                    new com.github.laxika.magicalvibes.service.target.TargetLegalityService(mockGameQueryService,
                            new com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService(mockGameQueryService),
                            mockTargetValidationService,
                            org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class)));
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

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler).handlePassPriority(any());
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
            }).when(mockMessageHandler).handlePlayCard(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }

        @Test
        @DisplayName("Hard AI detects cast success when ETB refills hand with a land (e.g. Explore)")
        void detectsCastSuccessWhenEtbRefillsHandWithLand() throws Exception {
            // Regression: Queen's Agent ETB triggers Explore which can refill hand with a land,
            // leaving hand.size() unchanged. The fix uses identity (hand.contains) not size.
            Card creature = new Card();
            creature.setName("Queen's Agent");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{5}{B}");
            creature.setPower(3);
            creature.setToughness(3);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.BLACK, 1);
            pool.add(ManaColor.COLORLESS, 5);

            Mockito.doAnswer(inv -> {
                List<Card> hand = mockGd.playerHands.get(mockAiPlayer.getId());
                hand.remove(creature);
                Card revealedLand = new Card();
                revealedLand.setName("Forest");
                revealedLand.setType(CardType.LAND);
                hand.add(revealedLand);
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }

        @Test
        @DisplayName("Hard AI still detects genuine silent failure when hand has other cards")
        void detectsGenuineFailureWhenHandHasOtherCards() throws Exception {
            Card creature = new Card();
            creature.setName("Test Bear");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{1}{G}");
            creature.setPower(2);
            creature.setToughness(2);
            Card sibling = new Card();
            sibling.setName("Other Card");
            sibling.setType(CardType.SORCERY);
            sibling.setManaCost("{10}{U}{U}");
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(sibling);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 1);
            pool.add(ManaColor.COLORLESS, 1);

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            verify(mockMessageHandler).handlePlayCard(any());
            verify(mockMessageHandler).handlePassPriority(any());
        }

        @Test
        @DisplayName("Hard AI does not throw when ETB refills hand with a null-cost land")
        void noExceptionWhenEtbRefillsHandWithNullCostLand() throws Exception {
            Card creature = new Card();
            creature.setName("Queen's Agent");
            creature.setType(CardType.CREATURE);
            creature.setManaCost("{5}{B}");
            creature.setPower(3);
            creature.setToughness(3);
            mockGd.playerHands.get(mockAiPlayer.getId()).add(creature);

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.BLACK, 1);
            pool.add(ManaColor.COLORLESS, 5);

            Mockito.doAnswer(inv -> {
                List<Card> hand = mockGd.playerHands.get(mockAiPlayer.getId());
                hand.remove(creature);
                Card revealedLand = new Card();
                revealedLand.setName("Forest");
                revealedLand.setType(CardType.LAND);
                hand.add(revealedLand);
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            assertThatCode(() -> createEngine().handleEvent(AiDecisionKind.GAME_STATE))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Hard AI builds damage assignments for divided damage spell")
        void buildsDamageAssignmentsForDividedDamageSpell() throws Exception {
            Card spell = new Card();
            spell.setName("Test Divided Damage");
            spell.setType(CardType.SORCERY);
            spell.setManaCost("{1}{R}");
            spell.target(null, 1, 3)
                    .addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongTargetCreatures(3));
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
            }).when(mockMessageHandler).handlePlayCard(any());

            HardAiDecisionEngine engine = createEngine();
            MCTSEngine failingMcts = Mockito.mock(MCTSEngine.class);
            Mockito.when(failingMcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                    .thenThrow(new RuntimeException("MCTS disabled for test"));
            engine.setMctsEngine(failingMcts);

            engine.handleEvent(AiDecisionKind.GAME_STATE);

            ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
            verify(mockMessageHandler).handlePlayCard(captor.capture());

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
                mockGd.interaction.beginInteraction(new PendingInteraction.ColorChoice(null, null, null, null, java.util.List.of(), "Choose a color."));
                return null;
            }).when(mockMessageHandler).handleTapPermanent(any());

            createEngine().handleEvent(AiDecisionKind.GAME_STATE);

            // AI should have tapped but NOT cast the spell or passed priority
            verify(mockMessageHandler).handleTapPermanent(any());
            verify(mockMessageHandler, never()).handlePlayCard(any());
            verify(mockMessageHandler, never()).handlePassPriority(any());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @DisplayName("Curving out mana efficiency")
    class CurvingOutManaEfficiency {

        @Mock private AiGameActions mockMessageHandler;
        @Mock private GameQueryService mockGameQueryService;
        @Mock private BlockLegalityService mockBlockLegalityService;
        @Mock private CombatAttackService mockCombatAttackService;
        @Mock private GameActionAvailabilityService mockGameActionAvailabilityService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingCostService mockCastingCostService;
        @Mock private com.github.laxika.magicalvibes.service.cast.CastingPermissionService mockCastingPermissionService;
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
            AiTestPlayabilityStub.install(mockGameActionAvailabilityService, mockCastingCostService, mockGameQueryService);
            HardAiDecisionEngine engine = new HardAiDecisionEngine(
                    mockGd.id, mockAiPlayer, mockGameRegistry, mockMessageHandler,
                    mockGameQueryService, mockBlockLegalityService, mockCombatAttackService, mockGameActionAvailabilityService,
                    mockCastingCostService, mockCastingPermissionService,
                    mockTargetValidationService,
                    new com.github.laxika.magicalvibes.service.target.TargetLegalityService(mockGameQueryService,
                            new com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService(mockGameQueryService),
                            mockTargetValidationService,
                            org.mockito.Mockito.mock(com.github.laxika.magicalvibes.service.effect.AmountEvaluationService.class)));
            return engine;
        }

        @Test
        @DisplayName("Hard AI prefers casting two cheaper spells over one expensive spell for better total value")
        void prefersTwoCheaperSpellsOverOneExpensive() throws Exception {
            // Big creature: cost 4, value = 3*3.0 + 3*1.5 = 13.5
            Card bigCreature = new Card();
            bigCreature.setName("Big Creature");
            bigCreature.setType(CardType.CREATURE);
            bigCreature.setManaCost("{3}{G}");
            bigCreature.setPower(3);
            bigCreature.setToughness(3);

            // Medium creature: cost 3, value = 2*3.0 + 3*1.5 = 10.5
            Card mediumCreature = new Card();
            mediumCreature.setName("Medium Creature");
            mediumCreature.setType(CardType.CREATURE);
            mediumCreature.setManaCost("{2}{G}");
            mediumCreature.setPower(2);
            mediumCreature.setToughness(3);

            // Small creature: cost 1, value = 2*3.0 + 1*1.5 = 7.5
            Card smallCreature = new Card();
            smallCreature.setName("Small Creature");
            smallCreature.setType(CardType.CREATURE);
            smallCreature.setManaCost("{G}");
            smallCreature.setPower(2);
            smallCreature.setToughness(1);

            // With 4 mana:
            // Big alone: 13.5 (uses all 4 mana)
            // Medium + Small: 10.5 + 7.5 = 18.0 (uses 3+1 = 4 mana)
            // AI should pick Medium first (starting the better sequence)
            mockGd.playerHands.get(mockAiPlayer.getId()).addAll(List.of(bigCreature, mediumCreature, smallCreature));

            ManaPool pool = mockGd.playerManaPools.get(mockAiPlayer.getId());
            pool.add(ManaColor.GREEN, 4);

            Mockito.doAnswer(inv -> {
                PlayCardRequest req = inv.getArgument(1);
                mockGd.playerHands.get(mockAiPlayer.getId()).remove(req.cardIndex());
                return null;
            }).when(mockMessageHandler).handlePlayCard(any());

            // Force MCTS to fail so the evaluator fallback (tryCastSpell) is exercised
            HardAiDecisionEngine engine = createEngine();
            MCTSEngine failingMcts = Mockito.mock(MCTSEngine.class);
            Mockito.when(failingMcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                    .thenThrow(new RuntimeException("MCTS disabled for test"));
            engine.setMctsEngine(failingMcts);

            engine.handleEvent(AiDecisionKind.GAME_STATE);

            ArgumentCaptor<PlayCardRequest> captor = ArgumentCaptor.forClass(PlayCardRequest.class);
            verify(mockMessageHandler).handlePlayCard(captor.capture());

            // Should cast Medium Creature (index 1) first, not Big Creature (index 0)
            PlayCardRequest request = captor.getValue();
            Card castCard = List.of(bigCreature, mediumCreature, smallCreature).get(request.cardIndex());
            assertThat(castCard.getName()).isEqualTo("Medium Creature");
        }
    }

    @Test
    @DisplayName("Hard AI casts Cryptic Command with its choose-two target")
    void castsCrypticCommandWithChooseTwoTarget() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());
        givePlayerIslands(player1, 4);

        Permanent target = new Permanent(new AirElemental());
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.setHand(player1, List.of(new CrypticCommand()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Cryptic Command");
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Hard AI does not cast Ajani's Response at an unaffordable untapped target")
    void doesNotCastTargetReducedSpellAtUnaffordableTarget() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        Permanent plains = new Permanent(new Plains());
        plains.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(plains);
        givePlayerIslands(player1, 2);

        Permanent ownTappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownTappedCreature.setSummoningSick(false);
        ownTappedCreature.tap();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setSummoningSick(false);
        harness.setHand(player1, List.of(new AjanisResponse()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Hard AI supplies both targets for Blinding Beam's tap mode")
    void castsBlindingBeamWithTwoTargetCreatures() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        for (int i = 0; i < 3; i++) {
            Permanent plains = new Permanent(new Plains());
            plains.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(plains);
        }

        Permanent firstTarget = new Permanent(new AirElemental());
        Permanent secondTarget = new Permanent(new AirElemental());
        gd.playerBattlefields.get(player2.getId()).add(firstTarget);
        gd.playerBattlefields.get(player2.getId()).add(secondTarget);
        harness.setHand(player1, List.of(new BlindingBeam()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Blinding Beam");
        assertThat(gd.stack.getFirst().getTargetId()).isNull();
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactlyInAnyOrder(firstTarget.getId(), secondTarget.getId());
    }

    @Test
    @DisplayName("Hard AI does not cast Steel Sabotage when no mode has valid targets")
    void doesNotCastSteelSabotageWhenNoValidMode() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        Permanent island = new Permanent(new Island());
        island.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(island);

        harness.setHand(player1, List.of(new SteelSabotage()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not cast Unbury when neither mode has a legal graveyard target")
    void doesNotCastUnburyWithoutCreatureInGraveyard() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        givePlayerSwamps(player1, 2);
        gd.playerGraveyards.get(player1.getId()).add(new HolyDay());
        harness.setHand(player1, List.of(new Unbury()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(permanent -> !permanent.isTapped());
        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(Unbury.class);
    }

    @Test
    @DisplayName("Hard AI skips Steel Sabotage (no valid mode) and casts another available spell")
    void skipsModalSpellAndCastsAlternative() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setMctsEngine(new MCTSEngine(HeadlessSimulationContext.getSimulator(), 42L, 500));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

        // Set up as opponent's turn, end step A?€�t good timing for REMOVAL instants
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        // This test guards the deterministic board-wipe evaluation (SpellEvaluator), not the
        // search: wiping a single bear is a thin cast-vs-pass margin for MCTS, whose search
        // varies run-to-run with map ordering of the game's random UUIDs A?€�t so force the
        // evaluator fallback instead.
        MCTSEngine failingMcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(failingMcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenThrow(new RuntimeException("MCTS disabled for test"));
        ai.setMctsEngine(failingMcts);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Slagstorm");
    }

    @Test
    @DisplayName("Hard AI casts Fit of Rage on an undersized opponent creature to enable Smite the Monstrous")
    void pumpsUndersizedCreatureToEnableSizeGatedRemoval() {
        HardAiDecisionEngine ai = createHardAi(player1);
        // Deterministic evaluator path A?€�t the cast-vs-pass margin for this two-spell
        // setup is thin for MCTS under UUID map-ordering noise.
        MCTSEngine failingMcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(failingMcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenThrow(new RuntimeException("MCTS disabled for test"));
        ai.setMctsEngine(failingMcts);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // Opponent's only creature is below Smite's power-4 gate; AI has no creatures
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setHand(player1, List.of(new FitOfRage(), new SmiteTheMonstrous()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // The critical combo setup: pump the undersized threat so Smite becomes legal
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Fit of Rage");
        assertThat(gd.stack.getFirst().getTargetId())
                .withFailMessage("Hard AI should Fit of Rage the opponent's 2/2 to enable Smite (power 4+), "
                        + "not refuse the cast or pump an empty board")
                .isEqualTo(oppBears.getId());
    }

    @Test
    @DisplayName("Hard AI limits attackers when attack tax is present")
    void limitsAttackersWhenAttackTaxPresent() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());

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
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));

        ai.handleEvent(AiDecisionKind.ATTACKER_DECLARATION);

        // At most 1 creature should be attacking (can only afford {1} tax)
        long attackingCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isAttacking)
                .count();
        assertThat(attackingCount).isLessThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Hard AI casts Skaab Ruinator when graveyard has 3 creature cards")
    void castsSkaabRuinatorWithThreeCreatures() {
        FakeConnection aiConn = new FakeConnection("ai-hard-test");
        harness.getSessionManager().registerPlayer(aiConn, player1.getId(), "Alice");
        HardAiDecisionEngine ai = new HardAiDecisionEngine(
                gd.id, player1, harness.getGameRegistry(),
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setMctsEngine(new MCTSEngine(HeadlessSimulationContext.getSimulator(), 42L, 500));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
                harness.getGameService(), harness.getGameQueryService(), harness.getBlockLegalityService(), harness.getCombatAttackService(),
                harness.getGameActionAvailabilityService(), harness.getCastingCostService(), harness.getCastingPermissionService(), harness.getTargetValidationService(), harness.getTargetLegalityService());
        ai.setMctsEngine(new MCTSEngine(HeadlessSimulationContext.getSimulator(), 42L, 500));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skaab Ruinator");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Holy Day"));
    }

}
