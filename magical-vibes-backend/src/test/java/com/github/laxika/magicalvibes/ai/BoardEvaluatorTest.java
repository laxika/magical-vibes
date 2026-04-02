package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BloodcrazedNeonate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    // ===== creature state adjustments =====

    @Nested
    @DisplayName("Creature state adjustments")
    class CreatureStateAdjustments {

        @BeforeEach
        void clearBoards() {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
        }

        @Test
        @DisplayName("Creature with marked damage scores lower than undamaged creature")
        void markedDamageReducesScore() {
            Permanent healthy = new Permanent(new GrizzlyBears());
            healthy.setSummoningSick(false);

            Permanent damaged = new Permanent(new GrizzlyBears());
            damaged.setSummoningSick(false);
            damaged.setMarkedDamage(1);

            double healthyScore = evaluator.creatureScore(gd, healthy, player1.getId(), player2.getId());
            double damagedScore = evaluator.creatureScore(gd, damaged, player1.getId(), player2.getId());

            // Damaged 2/2 with 1 damage = effective 2/1, should score lower
            assertThat(damagedScore).isLessThan(healthyScore);
        }

        @Test
        @DisplayName("Heavily damaged creature scores much lower")
        void heavilyDamagedCreatureScoresLower() {
            // Simulate a 4/4 with 3 damage — barely alive
            Permanent airElemental = new Permanent(new AirElemental());
            airElemental.setSummoningSick(false);
            airElemental.setMarkedDamage(3);

            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);

            double damagedAeScore = evaluator.creatureScore(gd, airElemental, player1.getId(), player2.getId());
            double bearsScore = evaluator.creatureScore(gd, bears, player1.getId(), player2.getId());

            // 4/4 with 3 damage → effective 4/1 + flying bonus
            // Should still be comparable to a 2/2 (not dominating like a healthy 4/4)
            assertThat(damagedAeScore).isLessThan(
                    evaluator.creatureScore(gd, new Permanent(new AirElemental()) {{
                        setSummoningSick(false);
                    }}, player1.getId(), player2.getId())
            );
        }

        @Test
        @DisplayName("Summoning-sick creature scores lower than one that can attack")
        void summoningSicknessReducesScore() {
            Permanent ready = new Permanent(new GrizzlyBears());
            ready.setSummoningSick(false);

            Permanent sick = new Permanent(new GrizzlyBears());
            sick.setSummoningSick(true);

            double readyScore = evaluator.creatureScore(gd, ready, player1.getId(), player2.getId());
            double sickScore = evaluator.creatureScore(gd, sick, player1.getId(), player2.getId());

            assertThat(sickScore).isLessThan(readyScore);
        }

        @Test
        @DisplayName("Tapped creature scores lower than untapped creature")
        void tappedCreatureScoresLower() {
            Permanent untapped = new Permanent(new GrizzlyBears());
            untapped.setSummoningSick(false);

            Permanent tapped = new Permanent(new GrizzlyBears());
            tapped.setSummoningSick(false);
            tapped.tap();

            double untappedScore = evaluator.creatureScore(gd, untapped, player1.getId(), player2.getId());
            double tappedScore = evaluator.creatureScore(gd, tapped, player1.getId(), player2.getId());

            assertThat(tappedScore).isLessThan(untappedScore);
        }

        @Test
        @DisplayName("Flying worth more when opponent has no flyers or reach")
        void flyingContextAwareScoring() {
            Permanent flyer = new Permanent(new AirElemental());
            flyer.setSummoningSick(false);

            // No opponent creatures — flying is unblockable
            double unblockableScore = evaluator.creatureScore(gd, flyer, player1.getId(), player2.getId());

            // Add an opponent flyer — flying can now be blocked
            Permanent oppFlyer = new Permanent(new AirElemental());
            oppFlyer.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppFlyer);

            double blockableScore = evaluator.creatureScore(gd, flyer, player1.getId(), player2.getId());

            // Unblockable flying should score higher than blockable flying
            assertThat(unblockableScore).isGreaterThan(blockableScore);
        }

        @Test
        @DisplayName("Tapped and summoning-sick penalties stack")
        void tappedAndSummoningSickStack() {
            Permanent ready = new Permanent(new GrizzlyBears());
            ready.setSummoningSick(false);

            Permanent tappedAndSick = new Permanent(new GrizzlyBears());
            tappedAndSick.setSummoningSick(true);
            tappedAndSick.tap();

            double readyScore = evaluator.creatureScore(gd, ready, player1.getId(), player2.getId());
            double penalizedScore = evaluator.creatureScore(gd, tappedAndSick, player1.getId(), player2.getId());

            // Both penalties should apply
            assertThat(penalizedScore).isLessThan(readyScore);
            assertThat(readyScore - penalizedScore).isEqualTo(3.5); // -2.0 summoning + -1.5 tapped
        }
    }

    // ===== creatureThreatScore =====

    @Nested
    @DisplayName("creatureThreatScore")
    class CreatureThreatScore {

        @BeforeEach
        void clearBoards() {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
        }

        @Test
        @DisplayName("Lord pumping allies scores higher threat than vanilla creature with better stats")
        void lordThreatHigherThanVanilla() {
            // Benalish Marshal is a 3/3 that gives other creatures you control +1/+1
            Permanent marshal = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());

            // Add 5 creatures for the lord to pump — this is the typical "wide board" scenario
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Air Elemental is a 4/4 flying — bigger body
            Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

            double marshalThreat = evaluator.creatureThreatScore(gd, marshal, player1.getId(), player2.getId());
            double airElementalThreat = evaluator.creatureThreatScore(gd, airElemental, player1.getId(), player2.getId());

            // Marshal lord bonus for 6 other creatures (5 bears + Air Elemental) makes it the higher-threat target
            // even with flying's enhanced unblockable bonus
            assertThat(marshalThreat).isGreaterThan(airElementalThreat);
        }

        @Test
        @DisplayName("Lord with no allies has same threat as base creature score")
        void lordAloneNoExtraBonus() {
            Permanent marshal = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());

            double threatScore = evaluator.creatureThreatScore(gd, marshal, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, marshal, player1.getId(), player2.getId());

            // No other creatures to pump, so lord bonus is 0
            assertThat(threatScore).isEqualTo(baseScore);
        }

        @Test
        @DisplayName("Flying creature has extra evasion threat when opponent has no flyers or reach")
        void flyingEvasionBonusWhenUnblockable() {
            Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

            // Player2 has no creatures — can't block flyers
            double threatScore = evaluator.creatureThreatScore(gd, airElemental, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, airElemental, player1.getId(), player2.getId());

            // Should get evasion context bonus because opponent has no flyers/reach
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Flying creature gets no extra evasion bonus when opponent has flyers")
        void flyingNoExtraBonusWhenOpponentHasFlyers() {
            Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
            // Opponent also has a flyer
            harness.addToBattlefield(player2, new AirElemental());

            double threatScore = evaluator.creatureThreatScore(gd, airElemental, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, airElemental, player1.getId(), player2.getId());

            // Opponent can block flyers, so no evasion context bonus
            assertThat(threatScore).isEqualTo(baseScore);
        }

        @Test
        @DisplayName("Growth creature (slith-type) has higher threat than vanilla")
        void growthCreatureThreat() {
            // Bloodcrazed Neonate: 2/1, whenever deals combat damage to player put a +1/+1 counter
            Permanent neonate = harness.addToBattlefieldAndReturn(player1, new BloodcrazedNeonate());

            double threatScore = evaluator.creatureThreatScore(gd, neonate, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, neonate, player1.getId(), player2.getId());

            // Growth bonus should make it higher than base
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Threat score is always at least as high as base creature score")
        void threatScoreNeverLowerThanBase() {
            Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, bears, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, bears, player1.getId(), player2.getId());

            assertThat(threatScore).isGreaterThanOrEqualTo(baseScore);
        }
    }
}
