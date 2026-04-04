package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.e.Eviscerate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class MCTSEngineTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameSimulator simulator;
    private MCTSEngine engine;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        simulator = new GameSimulator(harness.getGameQueryService());
        engine = new MCTSEngine(simulator, 42L);
    }

    @Test
    @DisplayName("Budget of 1 still returns a valid action")
    void budgetOneReturnsValidAction() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 1);

        assertThat(action).isNotNull();
        assertThat(action).isInstanceOfAny(
                SimulationAction.PlayCard.class,
                SimulationAction.PassPriority.class
        );
    }

    @Test
    @DisplayName("MCTS chooses to play a spell when mana is available")
    void mctsChoosesToPlaySpell() {
        // Offer choice between Grizzly Bears (2/2) and Serra Angel (4/4 flying vigilance)
        harness.setHand(player1, List.of(new GrizzlyBears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 100);

        // MCTS should choose to play a spell rather than pass
        assertThat(action).isNotNull();
        assertThat(action).isInstanceOf(SimulationAction.PlayCard.class);
    }

    @Test
    @DisplayName("Budget of 100 completes within reasonable time")
    void budgetCompletesInTime() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        long start = System.currentTimeMillis();
        SimulationAction action = engine.search(gd, player1.getId(), 100);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        assertThat(elapsed).isLessThan(5000); // Should complete well under 5 seconds
    }

    @Test
    @DisplayName("With only pass available, MCTS returns pass")
    void onlyPassAvailable() {
        harness.setHand(player1, List.of());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 50);

        assertThat(action).isInstanceOf(SimulationAction.PassPriority.class);
    }

    @Test
    @DisplayName("MCTS prefers casting removal over passing when it clears a blocker for an attack")
    void mctsPrefersCastingRemovalOverPassingWhenItClearsBlocker() {
        // Scenario: AI has a 3/3 attacker, opponent has a 3/3 blocker.
        // AI has removal (Eviscerate, {3}{B} sorcery, destroy target creature) but only black mana
        // (so Grizzly Bears is NOT castable — no green mana).
        // The MCTS tree has 2 options: PlayCard(removal) vs PassPriority.
        // With multi-turn lookahead, the rollout spans through combat:
        //   - Cast removal → blocker dies → 3/3 attacks unblocked → 3 damage to opponent
        //   - Pass → combat → 3/3 attacks → blocked by 3/3 → both trade, 0 damage
        // MCTS should prefer casting removal (PlayCard) because it enables a profitable attack.
        Card removalSpell = new Eviscerate();
        Card uncastableCreature = new GrizzlyBears(); // can't cast, no green mana
        harness.setHand(player1, List.of(removalSpell, uncastableCreature));

        // AI attacker: Hill Giant (3/3), not summoning sick
        Permanent aiAttacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        aiAttacker.setSummoningSick(false);

        // Opponent blocker: Hill Giant (3/3)
        Permanent oppBlocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        oppBlocker.setSummoningSick(false);

        // Only black mana — enough for Eviscerate ({3}{B}) but not for Grizzly Bears ({1}{G})
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        // Verify that the removal spell is enumerated as a legal action
        List<SimulationAction> legalActions = simulator.getLegalActions(gd, player1.getId());
        assertThat(legalActions).as("Legal actions should include PlayCard for removal")
                .anyMatch(a -> a instanceof SimulationAction.PlayCard);

        SimulationAction action = engine.search(gd, player1.getId(), 5000);

        // MCTS should prefer casting removal to clear the blocker, not passing
        assertThat(action).isInstanceOf(SimulationAction.PlayCard.class);
        SimulationAction.PlayCard playCard = (SimulationAction.PlayCard) action;
        assertThat(playCard.handIndex()).isEqualTo(0); // Eviscerate at index 0
    }

    @Test
    @DisplayName("MCTS prefers stronger creature when both are castable")
    void mctsPrefsStrongerCreatureWhenBothCastable() {
        // Both Serra Angel (4/4 flying vigilance) and Grizzly Bears (2/2) are castable.
        // Rollouts where Serra Angel is played deal 4 flying damage per turn;
        // rollouts where Bears is played deal only 2 (and can be blocked).
        // MCTS should clearly prefer Serra Angel.
        harness.setHand(player1, List.of(new SerraAngel(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 5000);

        assertThat(action).isInstanceOf(SimulationAction.PlayCard.class);
        SimulationAction.PlayCard playCard = (SimulationAction.PlayCard) action;
        assertThat(playCard.handIndex()).as("Should prefer Serra Angel (index 0) over Grizzly Bears (index 1)")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("MCTS selects removal over creature when opponent has a blocker (3-way decision)")
    void mctsSelectsRemovalOverCreatureWithBlocker() {
        // 3-way root decision: PlayCard(Eviscerate), PlayCard(Bears), PassPriority.
        // AI has no creatures on the battlefield; opponent has Hill Giant (3/3).
        // - Cast Eviscerate → kill Hill Giant → rollout casts Bears → Bears attacks unblocked → 2 damage
        // - Cast Bears → Bears on battlefield with summoning sickness → next turn blocked by 3/3 → trade
        // - Pass → nothing happens
        // Eviscerate into Bears is the best plan; requires depth-2 evaluation to see it.
        harness.setHand(player1, List.of(new Eviscerate(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        Permanent oppBlocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        oppBlocker.setSummoningSick(false);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        SimulationAction action = engine.search(gd, player1.getId(), 5000);

        assertThat(action).isInstanceOf(SimulationAction.PlayCard.class);
        SimulationAction.PlayCard playCard = (SimulationAction.PlayCard) action;
        assertThat(playCard.handIndex()).as("Should prefer Eviscerate (index 0) over Bears (index 1)")
                .isEqualTo(0);
    }

    @Test
    @DisplayName("Multi-turn lookahead completes within time budget with deeper rollouts")
    void multiTurnLookaheadCompletesInTime() {
        // Use a non-seeded engine here because this test validates the time budget
        MCTSEngine timedEngine = new MCTSEngine(simulator);

        // Verify that the deeper rollouts (spanning through combat) still complete on time
        Card removalSpell = new Eviscerate();
        Card creatureSpell = new GrizzlyBears();
        harness.setHand(player1, List.of(removalSpell, creatureSpell));

        Permanent aiAttacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        aiAttacker.setSummoningSick(false);
        harness.addToBattlefield(player2, new HillGiant());

        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        gd.stack.clear();

        long start = System.currentTimeMillis();
        SimulationAction action = timedEngine.search(gd, player1.getId(), 50000);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(action).isNotNull();
        // With the 1200ms time budget + overhead, should complete well under 5 seconds
        assertThat(elapsed).isLessThan(5000);
    }
}
