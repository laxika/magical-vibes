package com.github.laxika.magicalvibes.ai.simulation;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class RolloutPolicyTest {

    private GameTestHarness harness;
    private Player player1;
    private GameData gd;
    private MCTSEngine engine;
    private UUID playerId;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        gd = harness.getGameData();
        harness.skipMulligan();
        GameSimulator simulator = new GameSimulator(harness.getGameQueryService());
        engine = new MCTSEngine(simulator);
        playerId = player1.getId();
    }

    @Nested
    @DisplayName("scoreRolloutAction")
    class ScoreRolloutActionTests {

        @Test
        @DisplayName("PassPriority scores 0.1")
        void passPriorityScore() {
            double score = engine.scoreRolloutAction(gd, new SimulationAction.PassPriority(), playerId);
            assertThat(score).isEqualTo(0.1);
        }

        @Test
        @DisplayName("DeclareAttackers with attackers scores 5.0")
        void declareAttackersNonEmptyScore() {
            var action = new SimulationAction.DeclareAttackers(List.of(0, 1));
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(5.0);
        }

        @Test
        @DisplayName("DeclareAttackers empty scores 0.5")
        void declareAttackersEmptyScore() {
            var action = new SimulationAction.DeclareAttackers(List.of());
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(0.5);
        }

        @Test
        @DisplayName("DeclareBlockers with blockers scores 5.0")
        void declareBlockersNonEmptyScore() {
            var action = new SimulationAction.DeclareBlockers(List.of(new int[]{0, 1}));
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(5.0);
        }

        @Test
        @DisplayName("DeclareBlockers empty scores 0.5")
        void declareBlockersEmptyScore() {
            var action = new SimulationAction.DeclareBlockers(List.of());
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(0.5);
        }

        @Test
        @DisplayName("ActivateAbility scores 3.0")
        void activateAbilityScore() {
            var action = new SimulationAction.ActivateAbility(UUID.randomUUID(), 0, null);
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(3.0);
        }

        @Test
        @DisplayName("MayAbilityChoice accept scores 2.0, decline scores 0.5")
        void mayAbilityChoiceScores() {
            assertThat(engine.scoreRolloutAction(gd, new SimulationAction.MayAbilityChoice(true), playerId))
                    .isEqualTo(2.0);
            assertThat(engine.scoreRolloutAction(gd, new SimulationAction.MayAbilityChoice(false), playerId))
                    .isEqualTo(0.5);
        }

        @Test
        @DisplayName("ChooseCard, ChoosePermanent, ChooseColor score 1.0")
        void choiceActionsScore() {
            assertThat(engine.scoreRolloutAction(gd, new SimulationAction.ChooseCard(0), playerId))
                    .isEqualTo(1.0);
            assertThat(engine.scoreRolloutAction(gd, new SimulationAction.ChoosePermanent(UUID.randomUUID()), playerId))
                    .isEqualTo(1.0);
            assertThat(engine.scoreRolloutAction(gd, new SimulationAction.ChooseColor("W"), playerId))
                    .isEqualTo(1.0);
        }

        @Test
        @DisplayName("PlayCard scores at least 0.1 even for low-value spells")
        void playCardFlooredAt01() {
            harness.setHand(player1, List.of(new GrizzlyBears()));
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.forceActivePlayer(player1);

            var action = new SimulationAction.PlayCard(0, null, 0);
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isGreaterThanOrEqualTo(0.1);
        }

        @Test
        @DisplayName("PlayCard with invalid hand index returns 0.1")
        void playCardInvalidIndex() {
            harness.setHand(player1, List.of());
            var action = new SimulationAction.PlayCard(5, null, 0);
            double score = engine.scoreRolloutAction(gd, action, playerId);
            assertThat(score).isEqualTo(0.1);
        }

        @Test
        @DisplayName("Stronger creature scores higher than weaker creature")
        void strongerCreatureScoresHigher() {
            harness.setHand(player1, List.of(new SerraAngel(), new GrizzlyBears()));
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.forceActivePlayer(player1);

            double serraScore = engine.scoreRolloutAction(gd, new SimulationAction.PlayCard(0, null, 0), playerId);
            double bearsScore = engine.scoreRolloutAction(gd, new SimulationAction.PlayCard(1, null, 0), playerId);
            assertThat(serraScore).isGreaterThan(bearsScore);
        }
    }

    @Nested
    @DisplayName("softmaxSelect")
    class SoftmaxSelectTests {

        @Test
        @DisplayName("Single action always returned")
        void singleAction() {
            var action = new SimulationAction.PassPriority();
            var result = engine.softmaxSelect(List.of(action), new double[]{1.0}, new Random(42));
            assertThat(result).isSameAs(action);
        }

        @Test
        @DisplayName("Equal scores produce roughly uniform distribution")
        void equalScoresUniform() {
            var actions = List.<SimulationAction>of(
                    new SimulationAction.PassPriority(),
                    new SimulationAction.DeclareAttackers(List.of()),
                    new SimulationAction.DeclareAttackers(List.of(0))
            );
            double[] scores = {5.0, 5.0, 5.0};
            Random rng = new Random(123);

            int[] counts = new int[3];
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                SimulationAction selected = engine.softmaxSelect(actions, scores, rng);
                counts[actions.indexOf(selected)]++;
            }

            for (int count : counts) {
                assertThat((double) count / trials).isCloseTo(1.0 / 3.0, within(0.03));
            }
        }

        @Test
        @DisplayName("Large score gap makes low-scored action nearly impossible")
        void largeGapDominates() {
            var high = new SimulationAction.DeclareAttackers(List.of(0));
            var low = new SimulationAction.PassPriority();
            var actions = List.<SimulationAction>of(high, low);
            // Difference of 30 with temperature 6 → exp(30/6) ≈ 148x ratio
            double[] scores = {30.0, 0.0};
            Random rng = new Random(42);

            int highCount = 0;
            int trials = 5_000;
            for (int i = 0; i < trials; i++) {
                if (engine.softmaxSelect(actions, scores, rng) == high) highCount++;
            }

            assertThat((double) highCount / trials).isGreaterThan(0.99);
        }

        @Test
        @DisplayName("One-temperature difference gives e/(1+e) ≈ 73% vs 27% split")
        void oneTemperatureDifference() {
            var actionA = new SimulationAction.DeclareAttackers(List.of(0));
            var actionB = new SimulationAction.DeclareAttackers(List.of(1));
            var actions = List.<SimulationAction>of(actionA, actionB);
            // Difference of 6 with temperature 6 → e ≈ 2.72x ratio
            double[] scores = {12.0, 6.0};
            Random rng = new Random(99);

            int aCount = 0;
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                if (engine.softmaxSelect(actions, scores, rng) == actionA) aCount++;
            }

            // Expected: e/(1+e) ≈ 0.731
            assertThat((double) aCount / trials).isCloseTo(Math.E / (1.0 + Math.E), within(0.02));
        }

        @Test
        @DisplayName("Negative scores handled correctly (numerical stability)")
        void negativeScores() {
            var actionA = new SimulationAction.DeclareAttackers(List.of(0));
            var actionB = new SimulationAction.PassPriority();
            var actions = List.<SimulationAction>of(actionA, actionB);
            double[] scores = {-100.0, -106.0};
            Random rng = new Random(42);

            int aCount = 0;
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                if (engine.softmaxSelect(actions, scores, rng) == actionA) aCount++;
            }

            // Same 6-point difference → same ratio as the positive case
            assertThat((double) aCount / trials).isCloseTo(Math.E / (1.0 + Math.E), within(0.02));
        }
    }

    @Nested
    @DisplayName("selectRolloutAction")
    class SelectRolloutActionTests {

        @Test
        @DisplayName("Single action returned immediately without sampling")
        void singleAction() {
            var action = new SimulationAction.PassPriority();
            var result = engine.selectRolloutAction(gd, List.of(action), playerId, new Random(42));
            assertThat(result).isSameAs(action);
        }

        @Test
        @DisplayName("High-scored action picked more often than low-scored action")
        void highScoreDominates() {
            var attack = new SimulationAction.DeclareAttackers(List.of(0));  // score 5.0
            var pass = new SimulationAction.PassPriority();                   // score 0.1
            var actions = List.<SimulationAction>of(attack, pass);
            Random rng = new Random(42);

            int attackCount = 0;
            int trials = 5_000;
            for (int i = 0; i < trials; i++) {
                if (engine.selectRolloutAction(gd, actions, playerId, rng) == attack) attackCount++;
            }

            // Score 5.0 vs 0.1 with temp 6.0 → softmax gives ~69% for attack,
            // plus epsilon (5%) splits 50/50 → overall ~68%
            assertThat((double) attackCount / trials).isCloseTo(0.68, within(0.05));
        }

        @Test
        @DisplayName("Epsilon exploration ensures low-scored actions are still reachable")
        void epsilonEnsuresExploration() {
            var attack = new SimulationAction.DeclareAttackers(List.of(0));  // score 5.0
            var pass = new SimulationAction.PassPriority();                   // score 0.1
            var actions = List.<SimulationAction>of(attack, pass);
            Random rng = new Random(42);

            int passCount = 0;
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                if (engine.selectRolloutAction(gd, actions, playerId, rng) == pass) passCount++;
            }

            // PassPriority should be picked at least ~2.5% of the time
            // (epsilon=0.05 gives 2.5% from random + some from softmax tail)
            assertThat(passCount).isGreaterThan(trials / 50);
        }

        @Test
        @DisplayName("With castable spells, stronger spell is picked more often")
        void strongerSpellPreferred() {
            harness.setHand(player1, List.of(new SerraAngel(), new GrizzlyBears()));
            harness.addMana(player1, ManaColor.WHITE, 5);
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.forceActivePlayer(player1);

            var serra = new SimulationAction.PlayCard(0, null, 0);
            var bears = new SimulationAction.PlayCard(1, null, 0);
            var pass = new SimulationAction.PassPriority();
            var actions = List.<SimulationAction>of(serra, bears, pass);
            Random rng = new Random(42);

            int serraCount = 0;
            int bearsCount = 0;
            int trials = 10_000;
            for (int i = 0; i < trials; i++) {
                SimulationAction selected = engine.selectRolloutAction(gd, actions, playerId, rng);
                if (selected.equals(serra)) serraCount++;
                else if (selected.equals(bears)) bearsCount++;
            }

            // Serra Angel should be picked more often than Grizzly Bears
            assertThat(serraCount).isGreaterThan(bearsCount);
            // But Bears should still get picked sometimes (softmax exploration)
            assertThat(bearsCount).isGreaterThan(0);
        }
    }
}
