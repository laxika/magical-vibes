package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class BoardEvaluatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private BoardEvaluator evaluator;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        evaluator = new BoardEvaluator(harness.getGameQueryService());

        // Clear hands for deterministic evaluation
        gd.playerHands.get(player1.getId()).clear();
        gd.playerHands.get(player2.getId()).clear();
    }

    @Test
    @DisplayName("Empty board with equal life = symmetric score near 0")
    void emptyBoardSymmetric() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        double score1 = evaluator.evaluate(gd, player1.getId());
        double score2 = evaluator.evaluate(gd, player2.getId());

        assertThat(score1).isEqualTo(0.0);
        assertThat(score2).isEqualTo(0.0);
    }

    @Test
    @DisplayName("AI with 3/3 creature, opponent has nothing = positive score")
    void creatureAdvantagePositive() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        double score = evaluator.evaluate(gd, player1.getId());
        assertThat(score).isGreaterThan(0);
    }

    @Test
    @DisplayName("Both players have same creature = near zero creature differential")
    void sameCreatureNearZero() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        Permanent bears1 = new Permanent(new GrizzlyBears());
        bears1.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears1);

        Permanent bears2 = new Permanent(new GrizzlyBears());
        bears2.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears2);

        double score = evaluator.evaluate(gd, player1.getId());
        assertThat(score).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Life differential scoring")
    void lifeDifferential() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 10);

        double score = evaluator.evaluate(gd, player1.getId());
        // (20-10) * 2.0 = 20.0
        assertThat(score).isEqualTo(20.0);
    }

    @Test
    @DisplayName("Low-life bonus when opponent is at 5 or below")
    void lowLifeBonus() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 5);

        double score = evaluator.evaluate(gd, player1.getId());
        // Life diff: (20-5)*2=30, low-life bonus: +10 = 40
        assertThat(score).isEqualTo(40.0);
    }

    @Test
    @DisplayName("Low-life penalty when AI is at 5 or below")
    void lowLifePenalty() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        gd.playerLifeTotals.put(player1.getId(), 5);
        gd.playerLifeTotals.put(player2.getId(), 20);

        double score = evaluator.evaluate(gd, player1.getId());
        // Life diff: (5-20)*2=-30, low-life penalty: -10 = -40
        assertThat(score).isEqualTo(-40.0);
    }

    @Test
    @DisplayName("Flying creature scores higher than same P/T vanilla")
    void flyingCreatureScoresHigher() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);

        double bearsScore = evaluator.creatureScore(gd, bears, player1.getId(), player2.getId());

        // Air Elemental is 4/4 flying - should be higher
        Permanent airElemental = new Permanent(new AirElemental());
        airElemental.setSummoningSick(false);

        double aeScore = evaluator.creatureScore(gd, airElemental, player1.getId(), player2.getId());

        assertThat(aeScore).isGreaterThan(bearsScore);
    }

    @Test
    @DisplayName("Game over: opponent at 0 life = max score")
    void gameOverOpponentDead() {
        gd.playerLifeTotals.put(player1.getId(), 20);
        gd.playerLifeTotals.put(player2.getId(), 0);

        double score = evaluator.evaluate(gd, player1.getId());
        assertThat(score).isEqualTo(100000.0);
    }

    @Test
    @DisplayName("Game over: AI at 0 life = min score")
    void gameOverAiDead() {
        gd.playerLifeTotals.put(player1.getId(), 0);
        gd.playerLifeTotals.put(player2.getId(), 20);

        double score = evaluator.evaluate(gd, player1.getId());
        assertThat(score).isEqualTo(-100000.0);
    }

    @Test
    @DisplayName("Card advantage contributes to score")
    void cardAdvantage() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();

        // Give player1 more cards in hand
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());
        gd.playerHands.get(player1.getId()).add(new GrizzlyBears());

        double score = evaluator.evaluate(gd, player1.getId());
        // 2 cards * 6.0 = 12.0
        assertThat(score).isEqualTo(12.0);
    }

    @Test
    @DisplayName("SerraAngel (flying + vigilance) scores higher than vanilla bears")
    void vigilanceFlyingKeywordsBonus() {
        gd.playerBattlefields.get(player1.getId()).clear();

        Permanent serra = new Permanent(new SerraAngel());
        serra.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(serra);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);

        double serraScore = evaluator.creatureScore(gd, serra, player1.getId(), player2.getId());
        double bearsScore = evaluator.creatureScore(gd, bears, player1.getId(), player2.getId());

        // Serra (4/4 with flying +4, vigilance +2) vs Bears (2/2 vanilla)
        assertThat(serraScore).isGreaterThan(bearsScore);
    }
}
