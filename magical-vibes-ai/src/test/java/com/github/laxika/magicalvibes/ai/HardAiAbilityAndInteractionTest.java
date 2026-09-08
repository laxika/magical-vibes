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
class HardAiAbilityAndInteractionTest extends HardAiDecisionEngineTestSupport {

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
    @DisplayName("Hard AI activates Prodigal Pyromancer's tap ability to deal damage to opponent creature")
    void activatesProdigalPyromancerTapAbility() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, end step A?€�t good timing for "any time" abilities
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not activate A?€�t creature is summoning sick
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
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Prodigal Pyromancer untapped
        Permanent pyromancer = new Permanent(new com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(pyromancer);

        // No opponent creatures A?€�t should target opponent face
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Hard AI executes the exact Rod of Ruin target selected by ability MCTS")
    void executesRodTargetSelectedByAbilityMcts() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        Permanent rod = new Permanent(new RodOfRuin());
        gd.playerBattlefields.get(player1.getId()).add(rod);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new TragedyFeaster()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());

        MCTSEngine mcts = Mockito.mock(MCTSEngine.class);
        Mockito.when(mcts.search(any(), any(), Mockito.anyInt(), Mockito.anyList()))
                .thenReturn(new SimulationAction.ActivateAbility(
                        rod.getId(), 0, player2.getId()));
        ai.setMctsEngine(mcts);

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(rod.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Hard AI activates Shivan Dragon pump ability only during combat")
    void activatesShivanDragonPumpOnlyDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Declare blockers step A?€�t good timing for pump
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // Shivan Dragon with {R} available for pump
        Permanent dragon = new Permanent(new com.github.laxika.magicalvibes.cards.s.ShivanDragon());
        dragon.setSummoningSick(false);
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        // One untapped Mountain for mana
        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should activate pump during combat
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Hard AI does not activate pump ability during precombat main phase")
    void doesNotActivatePumpDuringMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Precombat main A?€�t not a good time for pump
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        Permanent dragon = new Permanent(new com.github.laxika.magicalvibes.cards.s.ShivanDragon());
        dragon.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(dragon);

        Permanent mountain = new Permanent(new Mountain());
        mountain.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT pump during main phase A?€�t waste of mana
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI activates Thrun regenerate ability during combat")
    void activatesThrunRegenerateDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Declare blockers A?€�t good timing for regenerate
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should NOT activate regenerate during main A?€�t save mana for casting
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
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // Thrun needs {1}{G} but we have no mana
        Permanent thrun = new Permanent(new com.github.laxika.magicalvibes.cards.t.ThrunTheLastTroll());
        thrun.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(thrun);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not activate A?€�t can't afford {1}{G}
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
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Mogg Fanatic (1/1, sacrifice: deal 1 damage to any target)
        Permanent mogg = new Permanent(new com.github.laxika.magicalvibes.cards.m.MoggFanatic());
        mogg.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mogg);

        // Opponent has a 5/5 creature A?€�t 1 damage won't kill it, sacrifice not worth it
        Permanent bigCreature = new Permanent(new AirElemental());
        bigCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bigCreature);

        // Sacrificing the Fanatic costs a creature and buys nothing the evaluator scores: it does
        // not count marked damage, so 1 damage to a 4/4 is worth zero. That margin is still thinner
        // than what a shuffled library hands the rollouts, and the search activated the ability
        // roughly one run in twelve until both decks were pinned.
        pinLibrariesAndHands();

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // AI at 2 life A?€�t paying 2 would kill it
        gd.playerLifeTotals.put(player1.getId(), 2);

        // Glorifier of Dusk (Pay 2 life: gain flying/vigilance)
        Permanent glorifier = new Permanent(new com.github.laxika.magicalvibes.cards.g.GlorifierOfDusk());
        glorifier.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(glorifier);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not activate A?€�t life cost check: life <= amount means can't pay
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
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not activate A?€�t permanent is already tapped
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
        gd.interaction.clearAwaitingInput();
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

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        // Llanowar Elves has a mana ability ({T}: Add {G})
        Permanent elves = new Permanent(new LlanowarElves());
        elves.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elves);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // The mana ability should be skipped A?€�t nothing on the stack
        assertThat(gd.stack).isEmpty();
        // Elves should NOT be tapped (mana ability was not attempted)
        assertThat(elves.isTapped()).isFalse();
    }

    private void giveOpponentPriority(Player opponent) {
        harness.forceActivePlayer(opponent);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(opponent.getId());
    }

    @Test
    @DisplayName("Hard AI casts flash creature at opponent's end step")
    void castsFlashCreatureAtOpponentsEndStep() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, end step A?€�t optimal timing for flash creatures
        giveOpponentPriority(player2);

        // Give AI 3 white mana for Benalish Knight ({2}{W}, 2/2 first strike flash)
        givePlayerPlains(player1, 3);

        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

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

        // Only a flash creature in hand A?€�t should be held for opponent's end step
        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should not cast A?€�t waiting for optimal timing on opponent's turn
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI holds mana for flash creature over low-value sorcery")
    void holdsManaForFlashCreatureOverLowValueSorcery() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Give AI 3 mana (2 plains + 1 green) A?€�t enough for either Bears or Knight but not both
        givePlayerPlains(player1, 2);
        givePlayerForests(player1, 1);

        // Hand: Grizzly Bears ({1}{G}, ~7 value) + Benalish Knight ({2}{W}, flash, held ~8*1.2 A?‰�? 9.6)
        // Benalish Knight's held value should exceed Bears value Ă— 0.8 factor A?†’ hold mana
        harness.setHand(player1, List.of(new GrizzlyBears(), new BenalishKnight()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should hold mana for the flash creature A?€�t stack stays empty
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI casts sorcery while reserving mana for flash creature when both fit")
    void castsSorceryAndReservesManaForFlashCreature() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        // Use postcombat main A?€�t simpler decision path (no MCTS combat evaluation)
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        // Give AI 5 mana (2 green + 3 white) A?€�t enough for Bears (2 mana) + Knight (3 mana)
        givePlayerForests(player1, 2);
        givePlayerPlains(player1, 3);

        // Hand: Grizzly Bears ({1}{G}, 2 mana) + Benalish Knight ({2}{W}, 3 mana, flash)
        // With 5 total mana, AI can cast Bears and still afford Knight later
        harness.setHand(player1, List.of(new GrizzlyBears(), new BenalishKnight()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // AI should cast the sorcery-speed creature (can still flash in the Knight later)
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

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

        // No attackers or blockers A?€�t no combat happening
        Card giantGrowth = new com.github.laxika.magicalvibes.cards.g.GiantGrowth();
        double value = ai.evaluateCombatTrickInCombat(gd, giantGrowth, false);

        assertThat(value).isEqualTo(-1);
    }

    @Test
    @DisplayName("Hard AI does not cast flash creature at opponent's precombat main")
    void doesNotCastFlashCreatureAtOpponentsPrecombatMain() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Set up as opponent's turn, precombat main A?€�t not a good time for flash creatures
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        givePlayerPlains(player1, 3);
        harness.setHand(player1, List.of(new BenalishKnight()));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not cast A?€�t precombat main is bad timing for FLASH_CREATURE
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPlaneswalkerToBattlefield(Player player, Card card, int loyalty) {
        Permanent perm = new Permanent(card);
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Hard AI activates untargeted +N loyalty ability during main phase")
    void activatesUntargetedPlusLoyaltyAbility() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Jace Beleren: +2 each player draws a card (no target)
        Permanent jace = addPlaneswalkerToBattlefield(player1, new com.github.laxika.magicalvibes.cards.j.JaceBeleren(), 3);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should activate +2 ability A?€�t puts ability on stack and pays loyalty
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 3 + 2
    }

    @Test
    @DisplayName("Hard AI activates targeted +N loyalty ability against opponent")
    void activatesTargetedPlusLoyaltyAbility() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Chandra Bold Pyromancer: +1 deal 2 damage to target player
        Permanent chandra = addPlaneswalkerToBattlefield(player1,
                new com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer(), 5);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 5 + 1
    }

    @Test
    @DisplayName("Hard AI activates -N loyalty ability when effect value justifies loyalty cost")
    void activatesMinusLoyaltyAbilityWhenWorthIt() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Garruk Wildspeaker with 5 loyalty: A?�?’1 create a 3/3 Beast token (no target)
        // The +1 is multi-target (untap two lands) so it's skipped, leaving A?�?’1 as best option
        Permanent garruk = addPlaneswalkerToBattlefield(player1,
                new com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker(), 5);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should activate A?�?’1 (create 3/3 token) A?€�t good value
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(4); // 5 - 1
    }

    @Test
    @DisplayName("Hard AI does not activate loyalty ability during opponent's turn")
    void doesNotActivateLoyaltyAbilityOnOpponentsTurn() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
        gd.priorityPassedBy.add(player2.getId());

        addPlaneswalkerToBattlefield(player1, new com.github.laxika.magicalvibes.cards.j.JaceBeleren(), 3);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Loyalty abilities require sorcery speed A?€�t can't activate on opponent's turn
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate loyalty ability during combat step")
    void doesNotActivateLoyaltyAbilityDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        addPlaneswalkerToBattlefield(player1, new com.github.laxika.magicalvibes.cards.j.JaceBeleren(), 3);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Loyalty abilities require main phase A?€�t can't activate during combat
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate loyalty ability when stack is not empty")
    void doesNotActivateLoyaltyAbilityWithNonEmptyStack() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Put something on the stack
        gd.stack.add(new StackEntry(new GrizzlyBears(), player2.getId()));

        addPlaneswalkerToBattlefield(player1, new com.github.laxika.magicalvibes.cards.j.JaceBeleren(), 3);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Loyalty abilities require empty stack A?€�t should not add another entry
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Hard AI does not pump idle Killer Bees merely because combat is in progress")
    void doesNotPumpIdleKillerBeesDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveCombatPriority(player1);
        addKillerBees(player1);
        givePlayerForests(player1, 3);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Forest")))
                .allMatch(permanent -> !permanent.isTapped());
    }

    @Test
    @DisplayName("Hard AI may pump Killer Bees when it is attacking")
    void pumpsAttackingKillerBeesDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveCombatPriority(player1);
        Permanent bees = addKillerBees(player1);
        bees.setAttacking(true);
        bees.setAttackTarget(player2.getId());
        givePlayerForests(player1, 1);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(bees.getId());
    }

    @Test
    @DisplayName("Hard AI may pump Killer Bees when it is blocking")
    void pumpsBlockingKillerBeesDuringCombat() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveCombatPriority(player2);
        gd.priorityPassedBy.add(player2.getId());
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        gd.playerBattlefields.get(player2.getId()).add(attacker);
        Permanent bees = addKillerBees(player1);
        bees.setBlocking(true);
        bees.addBlockingTargetId(attacker.getId());
        givePlayerForests(player1, 1);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(bees.getId());
    }

    @Test
    @DisplayName("Hard AI pumps Killer Bees only enough to survive targeted damage")
    void pumpsKillerBeesOnlyEnoughToSurviveTargetedDamage() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        Permanent bees = addKillerBees(player1);
        givePlayerForests(player1, 3);
        harness.setHand(player1, List.of());

        Shock shock = new Shock();
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, shock, player2.getId(),
                shock.getName(), shock.getEffects(EffectSlot.SPELL), 0, bees.getId(), null));

        ai.handleEvent(AiDecisionKind.GAME_STATE);
        ai.handleEvent(AiDecisionKind.GAME_STATE);
        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY))
                .hasSize(2);
    }

    @Test
    @DisplayName("Hard AI does not pump Killer Bees in response to destroy removal")
    void doesNotPumpKillerBeesAgainstDoomBlade() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);
        Permanent bees = addKillerBees(player1);
        givePlayerForests(player1, 3);
        harness.setHand(player1, List.of());

        DoomBlade doomBlade = new DoomBlade();
        gd.stack.add(new StackEntry(StackEntryType.INSTANT_SPELL, doomBlade, player2.getId(),
                doomBlade.getName(), doomBlade.getEffects(EffectSlot.SPELL), 0, bees.getId(), null));

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Doom Blade");
    }

    private Permanent addKillerBees(Player player) {
        Permanent bees = new Permanent(new KillerBees());
        bees.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bees);
        return bees;
    }

    private void giveCombatPriority(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();
    }

    @Test
    @DisplayName("Hard AI does not activate loyalty ability twice on same planeswalker")
    void doesNotActivateLoyaltyAbilityTwicePerTurn() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        Permanent jace = addPlaneswalkerToBattlefield(player1,
                new com.github.laxika.magicalvibes.cards.j.JaceBeleren(), 3);
        // Simulate that loyalty was already activated this turn
        jace.setLoyaltyActivationsThisTurn(1);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Once-per-turn limit reached A?€�t should not activate
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI does not activate -N ability with insufficient loyalty counters")
    void doesNotActivateMinusAbilityWithInsufficientLoyalty() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Garruk Wildspeaker with 0 loyalty can't afford -4 ultimate,
        // +1 is multi-target (skipped), -1 needs at least 1 counter
        // The only non-multi-target abilities are -1 (needs 1 loyalty) and -4 (needs 4 loyalty)
        // With 0 loyalty counters, -1 can't be paid either
        addPlaneswalkerToBattlefield(player1,
                new com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker(), 0);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Can't afford any negative loyalty cost with 0 counters
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI skips variable loyalty cost abilities (-X)")
    void skipsVariableLoyaltyCostAbility() {
        HardAiDecisionEngine ai = createHardAi(player1);
        giveAiPriority(player1);

        // Chandra Nalaar: +1 (targeted, damage to player/PW), -X (variable), -8 (ultimate)
        // With 2 loyalty: +1 is the only option since -X is skipped and -8 is unaffordable
        Permanent chandra = addPlaneswalkerToBattlefield(player1,
                new com.github.laxika.magicalvibes.cards.c.ChandraNalaar(), 2);
        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should activate +1 (not -X) A?€�t target opponent's face or a planeswalker
        assertThat(gd.stack).hasSize(1);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("Hard AI activates Spiketail Hatchling sacrifice to counter opponent's high-value creature spell")
    void activatesSpiketailHatchlingToCounterHighValueSpell() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // Opponent's turn, they just cast a spell
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // Opponent's Serra Angel (MV=5) on the stack
        SerraAngel angel = new SerraAngel();
        StackEntry opponentSpell = new StackEntry(angel, player2.getId());
        gd.stack.add(opponentSpell);

        // AI has Spiketail Hatchling on the battlefield (1/1 flyer with sacrifice-to-counter)
        Permanent hatchling = new Permanent(new com.github.laxika.magicalvibes.cards.s.SpiketailHatchling());
        hatchling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatchling);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // The AI should sacrifice the Hatchling to counter the Serra Angel
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        StackEntry abilityOnStack = gd.stack.getLast();
        assertThat(abilityOnStack.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(abilityOnStack.getTargetId()).isEqualTo(angel.getId());
    }

    @Test
    @DisplayName("Hard AI does not activate Spiketail Hatchling when no spells are on the stack")
    void doesNotActivateSpiketailHatchlingWithEmptyStack() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        // AI has Spiketail Hatchling on the battlefield
        Permanent hatchling = new Permanent(new com.github.laxika.magicalvibes.cards.s.SpiketailHatchling());
        hatchling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatchling);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // No spell to counter A?€�t should not sacrifice the Hatchling
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hard AI saves Spiketail Hatchling when board is strong and threat is mediocre")
    void savesSpiketailHatchlingWhenBoardStrongAndThreatMediocre() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // AI has a strong board A?€�t Serra Angels
        for (int i = 0; i < 3; i++) {
            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);
        }

        // Opponent casts a mediocre creature (GrizzlyBears MV=2, 2/2)
        GrizzlyBears bears = new GrizzlyBears();
        StackEntry lowValueSpell = new StackEntry(bears, player2.getId());
        gd.stack.add(lowValueSpell);

        // AI has Spiketail Hatchling
        Permanent hatchling = new Permanent(new com.github.laxika.magicalvibes.cards.s.SpiketailHatchling());
        hatchling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatchling);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Threat reservation: strong board + mediocre threat A?†’ AI passes priority instead
        // of sacrificing the Hatchling. Hatchling should still be on the battlefield.
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(aiBattlefield.stream().anyMatch(p ->
                p.getCard().getName().equals("Spiketail Hatchling"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI does not activate Spiketail Hatchling to counter own spells")
    void doesNotActivateSpiketailHatchlingOnOwnSpells() {
        HardAiDecisionEngine ai = createHardAi(player1);

        // It's the opponent's turn A?€�t AI's spell is on the stack from a previous interaction
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        // AI's own spell on the stack
        GrizzlyBears bears = new GrizzlyBears();
        StackEntry ownSpell = new StackEntry(bears, player1.getId());
        gd.stack.add(ownSpell);

        Permanent hatchling = new Permanent(new com.github.laxika.magicalvibes.cards.s.SpiketailHatchling());
        hatchling.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(hatchling);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Should not sacrifice to counter own spell A?€�t Hatchling stays on battlefield
        List<Permanent> aiBattlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(aiBattlefield.stream().anyMatch(p ->
                p.getCard().getName().equals("Spiketail Hatchling"))).isTrue();
    }

    @Test
    @DisplayName("Hard AI sacs fodder to Viscera Seer for Scry before Wrath of God resolves")
    void sacrificesToVisceraSeerBeforeBoardWipe() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();

        WrathOfGod wrath = new WrathOfGod();
        gd.stack.add(new StackEntry(StackEntryType.SORCERY_SPELL, wrath, player2.getId(),
                wrath.getName(), wrath.getEffects(EffectSlot.SPELL), 0));

        Permanent seer = new Permanent(new com.github.laxika.magicalvibes.cards.v.VisceraSeer());
        seer.setSummoningSick(false);
        Permanent ornithopter = new Permanent(new com.github.laxika.magicalvibes.cards.o.Ornithopter());
        ornithopter.setSummoningSick(false);
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seer);
        gd.playerBattlefields.get(player1.getId()).add(ornithopter);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);
        if (gd.interaction.isAwaitingInput()) {
            ai.handleEvent(AiDecisionKind.INTERACTION);
        }

        // Sac cost paid immediately A?€�t Ornithopter (cheapest non-outlet) should be gone
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(p -> p.getId().equals(ornithopter.getId()))).isTrue();
        // Viscera Seer kept as the outlet
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(p -> p.getId().equals(seer.getId()))).isTrue();
        // Scry ability is on the stack above Wrath
        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Wrath of God");
    }

    @Test
    @DisplayName("Hard AI does not sac a healthy flyer to Viscera Seer for Scry with empty stack")
    void doesNotSacrificeToVisceraSeerWithEmptyStack() {
        HardAiDecisionEngine ai = createHardAi(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        gd.priorityPassedBy.add(player2.getId());
        gd.status = GameStatus.RUNNING;
        gd.interaction.clearAwaitingInput();
        gd.stack.clear();

        Permanent seer = new Permanent(new com.github.laxika.magicalvibes.cards.v.VisceraSeer());
        seer.setSummoningSick(false);
        Permanent ornithopter = new Permanent(new com.github.laxika.magicalvibes.cards.o.Ornithopter());
        ornithopter.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(seer);
        gd.playerBattlefields.get(player1.getId()).add(ornithopter);

        harness.setHand(player1, List.of());

        ai.handleEvent(AiDecisionKind.GAME_STATE);

        // Flying Ornithopter is worth more than Scry 1 when not doomed
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Nested
    @DisplayName("May Ability Choice")
    class MayAbilityChoiceTests {

        @Test
        @DisplayName("Hard AI accepts may ability with positive-value effects (e.g. draw card)")
        void acceptsMayAbilityWithPositiveValue() {
            HardAiDecisionEngine ai = createHardAi(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            gd.status = GameStatus.RUNNING;

            // Create a may ability that draws a card (positive value)
            Card sourceCard = new GrizzlyBears();
            com.github.laxika.magicalvibes.model.PendingMayAbility pending =
                    new com.github.laxika.magicalvibes.model.PendingMayAbility(
                            sourceCard, player1.getId(),
                            List.of(new com.github.laxika.magicalvibes.model.effect.DrawCardEffect(1)),
                            "You may draw a card");
            gd.pendingMayAbilities.add(pending);
            gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(player1.getId(), pending.description(), pending.manaCost()));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // The may ability should have been accepted (pending list consumed)
            assertThat(gd.pendingMayAbilities).isEmpty();
        }

        @Test
        @DisplayName("Hard AI declines may ability with purely negative effects (e.g. pay life)")
        void declinesMayAbilityWithNegativeValue() {
            HardAiDecisionEngine ai = createHardAi(player1);

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            gd.status = GameStatus.RUNNING;

            // Create a may ability that only deals damage to the AI (negative value)
            Card sourceCard = new GrizzlyBears();
            com.github.laxika.magicalvibes.model.PendingMayAbility pending =
                    new com.github.laxika.magicalvibes.model.PendingMayAbility(
                            sourceCard, player1.getId(),
                            List.of(new com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect(5, com.github.laxika.magicalvibes.model.effect.DamageRecipient.CONTROLLER)),
                            "You may have this deal 5 damage to you");
            gd.pendingMayAbilities.add(pending);
            gd.interaction.beginInteraction(new PendingInteraction.MayAbilityChoice(player1.getId(), pending.description(), pending.manaCost()));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // The may ability should have been declined (pending list consumed but effect not applied)
            assertThat(gd.pendingMayAbilities).isEmpty();
        }
    }

    @Nested
    @DisplayName("Scry Choice")
    class ScryChoiceTests {

        @Test
        @DisplayName("Hard AI keeps lands on top when low on mana sources")
        void keepsLandsOnTopWhenLowOnMana() {
            HardAiDecisionEngine ai = createHardAi(player1);

            // Only 2 lands on battlefield A?€�t AI needs more mana
            gd.playerBattlefields.get(player1.getId()).clear();
            Permanent island1 = new Permanent(new Island());
            island1.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island1);
            Permanent island2 = new Permanent(new Island());
            island2.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(island2);

            // Hand has an expensive spell
            harness.setHand(player1, List.of(new SerraAngel()));

            gd.status = GameStatus.RUNNING;

            // Set up scry with a land card
            Card landCard = new Island();
            Card spellCard = new GrizzlyBears();
            gd.interaction.beginInteraction(
                    new PendingInteraction.Scry(player1.getId(), List.of(landCard, spellCard)));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // Both cards should go on top (land is needed, spell is useful)
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("Hard AI puts lands on bottom when mana-flooded and no expensive spells")
        void putsLandsOnBottomWhenFlooded() {
            HardAiDecisionEngine ai = createHardAi(player1);

            // 7+ lands on battlefield A?€�t AI is flooded
            gd.playerBattlefields.get(player1.getId()).clear();
            for (int i = 0; i < 8; i++) {
                Permanent island = new Permanent(new Island());
                island.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(island);
            }

            // Hand has only cheap spells
            harness.setHand(player1, List.of(new GrizzlyBears()));

            gd.status = GameStatus.RUNNING;

            // Set up scry with a land card
            Card landCard = new Island();
            Card spellCard = new EliteVanguard();
            gd.interaction.beginInteraction(
                    new PendingInteraction.Scry(player1.getId(), List.of(landCard, spellCard)));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // Scry completed (land on bottom, spell on top)
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @Nested
    @DisplayName("Creature Type Choice")
    class CreatureTypeChoiceTests {

        @Test
        @DisplayName("Hard AI picks most common creature type from battlefield")
        void picksMostCommonCreatureType() {
            HardAiDecisionEngine ai = createHardAi(player1);

            gd.status = GameStatus.RUNNING;

            // Add 3 Elves and 1 Human to the battlefield
            for (int i = 0; i < 3; i++) {
                Permanent elf = new Permanent(new LlanowarElves());
                elf.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(elf);
            }
            Permanent human = new Permanent(new EliteVanguard());
            human.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(human);

            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player1.getId(), null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.SubtypeChoice(null),
                    List.of("ELF", "HUMAN"), "Choose a creature type."));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // The choice should be processed
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }

        @Test
        @DisplayName("Hard AI falls back to an offered type when its preferred type is restricted")
        void picksOfferedCreatureTypeWhenPreferredTypeIsRestricted() {
            HardAiDecisionEngine ai = createHardAi(player1);

            gd.status = GameStatus.RUNNING;

            for (int i = 0; i < 3; i++) {
                Permanent human = new Permanent(new EliteVanguard());
                human.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(human);
            }

            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player1.getId(), null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.SubtypeChoice(null),
                    List.of("ELEMENTAL", "ELF"), "Choose a creature type."));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @Nested
    @DisplayName("Basic Land Type Choice")
    class BasicLandTypeChoiceTests {

        @Test
        @DisplayName("Hard AI picks the basic land type it needs most based on color demand")
        void picksBasicLandTypeMatchingColorDemand() {
            HardAiDecisionEngine ai = createHardAi(player1);

            gd.status = GameStatus.RUNNING;

            // Battlefield: only Islands (blue mana)
            gd.playerBattlefields.get(player1.getId()).clear();
            for (int i = 0; i < 4; i++) {
                Permanent island = new Permanent(new Island());
                island.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(island);
            }

            // Hand: spells needing white mana (SerraAngel needs {3}{W}{W})
            harness.setHand(player1, List.of(new SerraAngel()));

            gd.interaction.beginInteraction(new PendingInteraction.ColorChoice(
                    player1.getId(), null, null,
                    new com.github.laxika.magicalvibes.model.ChoiceContext.BasicLandTypeChoice(null),
                    List.of("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST"),
                    "Choose a basic land type."));

            ai.handleEvent(AiDecisionKind.INTERACTION);

            // The choice should be processed (AI should pick PLAINS for white mana demand)
            assertThat(gd.interaction.isAwaitingInput()).isFalse();
        }
    }

    @Nested
    @DisplayName("May-Pay Trigger Economics")
    class MayPayTriggerTests {

        /** Opponent casts a red spell so Iron Star's trigger resolves into the may-pay prompt. */
        private void fireIronStarTriggerFromOpponent() {
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new BogardanFirefiend()));
            harness.addMana(player2, ManaColor.RED, 3);
            harness.castCreature(player2, 0);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Hard AI taps a spare land to pay for Iron Star's trigger when the mana has no other use")
        void paysMayPayTriggerWithSpareMana() {
            HardAiDecisionEngine ai = createHardAi(player1);
            gd.status = GameStatus.RUNNING;

            harness.addToBattlefield(player1, new IronStar());
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(mountain);
            harness.setHand(player1, List.of());

            int lifeBefore = gd.playerLifeTotals.get(player1.getId());
            fireIronStarTriggerFromOpponent();

            ai.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
            assertThat(mountain.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Hard AI declines Iron Star's trigger when paying would deny a held instant")
        void declinesMayPayTriggerWhenPaymentDeniesACast() {
            HardAiDecisionEngine ai = createHardAi(player1);
            gd.status = GameStatus.RUNNING;

            harness.addToBattlefield(player1, new IronStar());
            Permanent mountain = new Permanent(new Mountain());
            mountain.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(mountain);
            // A castable Shock and exactly one mana source A?€�t paying {1} would deny the Shock
            harness.setHand(player1, List.of(new Shock()));

            int lifeBefore = gd.playerLifeTotals.get(player1.getId());
            fireIronStarTriggerFromOpponent();

            ai.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
            assertThat(mountain.isTapped()).isFalse();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        }

        @Test
        @DisplayName("Hard AI declines its own-turn trigger when paying would starve a main-phase cast")
        void declinesOwnTurnTriggerWhenPaymentStarvesMainPhaseCast() {
            HardAiDecisionEngine ai = createHardAi(player1);
            gd.status = GameStatus.RUNNING;

            harness.addToBattlefield(player1, new IronStar());
            for (int i = 0; i < 2; i++) {
                Permanent mountain = new Permanent(new Mountain());
                mountain.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(mountain);
            }

            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            // Cast the AI's own red spell off floating mana; the Goblin Piker ({1}{R})
            // stays in hand and needs both untapped Mountains, so the {1} is not spare.
            harness.setHand(player1, List.of(new BogardanFirefiend(), new GoblinPiker()));
            harness.addMana(player1, ManaColor.RED, 3);

            int lifeBefore = gd.playerLifeTotals.get(player1.getId());
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());

            ai.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        }

        @Test
        @DisplayName("Hard AI floats spare mana to pay a pay-X prompt (Vigil for the Lost)")
        void floatsSpareManaForPayXPrompt() {
            HardAiDecisionEngine ai = createHardAi(player1);
            gd.status = GameStatus.RUNNING;

            harness.addToBattlefield(player1, new VigilForTheLost());
            harness.addToBattlefield(player1, new GrizzlyBears());
            for (int i = 0; i < 2; i++) {
                Permanent mountain = new Permanent(new Mountain());
                mountain.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(mountain);
            }
            harness.setHand(player1, List.of());

            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new CruelEdict()));
            harness.addMana(player2, ManaColor.BLACK, 2);

            int lifeBefore = gd.playerLifeTotals.get(player1.getId());
            harness.castSorcery(player2, 0, player1.getId());
            harness.passBothPriorities(); // Edict resolves, creature dies, Vigil triggers
            harness.passBothPriorities(); // trigger resolves -> X payment prompt
            assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();

            ai.handleEvent(AiDecisionKind.INTERACTION);

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        }
    }
}
