package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AbattoirGhoul;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.b.BenalishMarshal;
import com.github.laxika.magicalvibes.cards.b.BlindZealot;
import com.github.laxika.magicalvibes.cards.b.BloodcrazedNeonate;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.b.BogWraith;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SeveredLegion;
import com.github.laxika.magicalvibes.cards.v.ViashinoRunner;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

    // ===== Mana tempo scoring =====

    @Nested
    @DisplayName("Mana tempo scoring")
    class ManaTempoScoring {

        @BeforeEach
        void clearBoards() {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
        }

        @Test
        @DisplayName("manaTempoScore returns 0 when both players have equal mana sources")
        void equalManaSourcesZeroScore() {
            assertThat(BoardEvaluator.manaTempoScore(3, 3)).isEqualTo(0.0);
            assertThat(BoardEvaluator.manaTempoScore(0, 0)).isEqualTo(0.0);
            assertThat(BoardEvaluator.manaTempoScore(7, 7)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("manaTempoScore is positive when AI has more mana sources")
        void moreManaSourcesPositive() {
            assertThat(BoardEvaluator.manaTempoScore(5, 3)).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("manaTempoScore is negative when opponent has more mana sources")
        void lessManaSourcesNegative() {
            assertThat(BoardEvaluator.manaTempoScore(3, 5)).isLessThan(0.0);
        }

        @Test
        @DisplayName("Early mana gap (3 vs 5) is worth more than late mana gap (8 vs 10)")
        void diminishingReturns() {
            double earlyGap = BoardEvaluator.manaTempoScore(5, 3);
            double lateGap = BoardEvaluator.manaTempoScore(10, 8);

            assertThat(earlyGap).isGreaterThan(lateGap);
        }

        @Test
        @DisplayName("Mana advantage contributes positively to board evaluation")
        void manaAdvantageInBoardEvaluation() {
            // Give player1 five lands, player2 three lands
            for (int i = 0; i < 5; i++) {
                Permanent land = new Permanent(new Forest());
                land.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(land);
            }
            for (int i = 0; i < 3; i++) {
                Permanent land = new Permanent(new Forest());
                land.setSummoningSick(false);
                gd.playerBattlefields.get(player2.getId()).add(land);
            }

            double score = evaluator.evaluate(gd, player1.getId());
            assertThat(score).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("Non-land mana sources (mana dorks) count as mana sources")
        void manaDorksCountAsManaSource() {
            // Player1 has 3 lands + 1 mana dork = 4 mana sources
            for (int i = 0; i < 3; i++) {
                Permanent land = new Permanent(new Plains());
                land.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(land);
            }
            Permanent elf = new Permanent(new LlanowarElves());
            elf.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(elf);

            // Player2 has 3 lands = 3 mana sources
            for (int i = 0; i < 3; i++) {
                Permanent land = new Permanent(new Plains());
                land.setSummoningSick(false);
                gd.playerBattlefields.get(player2.getId()).add(land);
            }

            double scoreWithDork = evaluator.evaluate(gd, player1.getId());

            // Now remove the dork and give player1 only 3 lands (equal to opponent)
            gd.playerBattlefields.get(player1.getId()).remove(elf);

            double scoreWithout = evaluator.evaluate(gd, player1.getId());

            // The mana dork should contribute to a higher score (both as creature and mana source)
            assertThat(scoreWithDork).isGreaterThan(scoreWithout);
        }

        @Test
        @DisplayName("manaTempoScore is antisymmetric")
        void antisymmetric() {
            double score5v3 = BoardEvaluator.manaTempoScore(5, 3);
            double score3v5 = BoardEvaluator.manaTempoScore(3, 5);

            assertThat(score5v3).isEqualTo(-score3v5);
        }
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

        @Test
        @DisplayName("Cant-be-blocked creature has extra evasion threat")
        void cantBeBlockedEvasionBonus() {
            Permanent phantom = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

            double threatScore = evaluator.creatureThreatScore(gd, phantom, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, phantom, player1.getId(), player2.getId());

            // Should get evasion context bonus because it can't be blocked
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Fear creature has extra evasion threat when opponent has no black or artifact creatures")
        void fearEvasionBonusWhenUnblockable() {
            // Severed Legion: 2/2 black creature with fear
            Permanent legion = harness.addToBattlefieldAndReturn(player1, new SeveredLegion());

            // Opponent has only green creatures (can't block fear)
            harness.addToBattlefield(player2, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, legion, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, legion, player1.getId(), player2.getId());

            // Fear is effectively unblockable vs green creatures → extra evasion bonus
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Fear creature gets no extra evasion bonus when opponent has black creature")
        void fearNoExtraBonusWhenOpponentHasBlack() {
            Permanent legion = harness.addToBattlefieldAndReturn(player1, new SeveredLegion());

            // Opponent has a black creature that can block fear
            harness.addToBattlefield(player2, new AbattoirGhoul());

            double threatScore = evaluator.creatureThreatScore(gd, legion, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, legion, player1.getId(), player2.getId());

            // Opponent can block fear → no extra evasion bonus
            assertThat(threatScore).isEqualTo(baseScore);
        }

        @Test
        @DisplayName("Fear creature gets no extra evasion bonus when opponent has artifact creature")
        void fearNoExtraBonusWhenOpponentHasArtifact() {
            Permanent legion = harness.addToBattlefieldAndReturn(player1, new SeveredLegion());

            // Opponent has an artifact creature that can block fear
            harness.addToBattlefield(player2, new DarksteelMyr());

            double threatScore = evaluator.creatureThreatScore(gd, legion, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, legion, player1.getId(), player2.getId());

            // Artifact creature can block fear → no extra evasion bonus
            assertThat(threatScore).isEqualTo(baseScore);
        }

        @Test
        @DisplayName("Intimidate creature has extra evasion threat when opponent has no same-color or artifact creatures")
        void intimidateEvasionBonusWhenUnblockable() {
            // Blind Zealot: 2/2 black creature with intimidate
            Permanent zealot = harness.addToBattlefieldAndReturn(player1, new BlindZealot());

            // Opponent has only green creatures (can't block intimidate from a black creature)
            harness.addToBattlefield(player2, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, zealot, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, zealot, player1.getId(), player2.getId());

            // Intimidate is effectively unblockable vs different-color creatures → extra evasion bonus
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Menace creature has extra evasion threat when opponent has 1 or fewer creatures")
        void menaceEvasionBonusWhenFewBlockers() {
            // Viashino Runner: 3/2 with menace
            Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new ViashinoRunner());

            // Opponent has only 1 creature — not enough for menace
            harness.addToBattlefield(player2, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, arsonist, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, arsonist, player1.getId(), player2.getId());

            // Menace needs 2 blockers, opponent has ≤1 → extra evasion bonus
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Menace creature gets no extra evasion bonus when opponent has 2+ creatures")
        void menaceNoExtraBonusWhenEnoughBlockers() {
            Permanent arsonist = harness.addToBattlefieldAndReturn(player1, new ViashinoRunner());

            // Opponent has 2 creatures — enough to block menace
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, arsonist, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, arsonist, player1.getId(), player2.getId());

            // Enough blockers for menace → no extra evasion bonus
            assertThat(threatScore).isEqualTo(baseScore);
        }

        @Test
        @DisplayName("Swampwalk creature has extra evasion threat when opponent controls a swamp")
        void landwalkEvasionBonusWhenOpponentHasMatchingLand() {
            // Bog Wraith: 3/3 with swampwalk
            Permanent bogWraith = harness.addToBattlefieldAndReturn(player1, new BogWraith());

            // Opponent controls a swamp → swampwalk makes it unblockable
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(swamp);

            double threatScore = evaluator.creatureThreatScore(gd, bogWraith, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, bogWraith, player1.getId(), player2.getId());

            // Swampwalk is effectively unblockable when opponent has a swamp
            assertThat(threatScore).isGreaterThan(baseScore);
        }

        @Test
        @DisplayName("Swampwalk creature gets no extra evasion bonus when opponent has no swamps")
        void landwalkNoExtraBonusWhenNoMatchingLand() {
            Permanent bogWraith = harness.addToBattlefieldAndReturn(player1, new BogWraith());

            // Opponent has no swamps
            harness.addToBattlefield(player2, new GrizzlyBears());

            double threatScore = evaluator.creatureThreatScore(gd, bogWraith, player1.getId(), player2.getId());
            double baseScore = evaluator.creatureScore(gd, bogWraith, player1.getId(), player2.getId());

            // No matching land → no extra evasion bonus
            assertThat(threatScore).isEqualTo(baseScore);
        }
    }

    // ===== Keyword bonus context awareness =====

    @Nested
    @DisplayName("Context-aware keyword bonuses")
    class KeywordBonusContext {

        @BeforeEach
        void clearBoards() {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
        }

        @Test
        @DisplayName("Cant-be-blocked creature scores higher than same P/T vanilla")
        void cantBeBlockedScoresHigher() {
            // Phantom Warrior: 2/2 cant-be-blocked
            Permanent phantom = new Permanent(new PhantomWarrior());
            phantom.setSummoningSick(false);

            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);

            double phantomScore = evaluator.creatureScore(gd, phantom, player1.getId(), player2.getId());
            double bearsScore = evaluator.creatureScore(gd, bears, player1.getId(), player2.getId());

            // Cant-be-blocked should add bonus to keyword score
            assertThat(phantomScore).isGreaterThan(bearsScore);
        }

        @Test
        @DisplayName("Fear creature scores higher when opponent has no black or artifact creatures")
        void fearContextAwareScoring() {
            // Severed Legion: 2/2 with fear
            Permanent legion = new Permanent(new SeveredLegion());
            legion.setSummoningSick(false);

            // With only green opponent creatures — fear is effectively unblockable
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new GrizzlyBears()));
            double unblockableScore = evaluator.creatureScore(gd, legion, player1.getId(), player2.getId());

            // Now add a black creature — fear can be blocked
            gd.playerBattlefields.get(player2.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new AbattoirGhoul()));
            double blockableScore = evaluator.creatureScore(gd, legion, player1.getId(), player2.getId());

            // Fear should score higher when opponent can't block it
            assertThat(unblockableScore).isGreaterThan(blockableScore);
        }

        @Test
        @DisplayName("Intimidate creature scores higher when opponent has no same-color or artifact creatures")
        void intimidateContextAwareScoring() {
            // Blind Zealot: 2/2 black creature with intimidate
            Permanent zealot = new Permanent(new BlindZealot());
            zealot.setSummoningSick(false);

            // With only green opponent creatures — intimidate (black) is unblockable
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new GrizzlyBears()));
            double unblockableScore = evaluator.creatureScore(gd, zealot, player1.getId(), player2.getId());

            // Now add an artifact creature — can block intimidate
            gd.playerBattlefields.get(player2.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new DarksteelMyr()));
            double blockableScore = evaluator.creatureScore(gd, zealot, player1.getId(), player2.getId());

            // Intimidate should score higher when opponent can't block it
            assertThat(unblockableScore).isGreaterThan(blockableScore);
        }

        @Test
        @DisplayName("Menace creature scores higher when opponent has 1 or fewer creatures")
        void menaceContextAwareScoring() {
            // Viashino Runner: 3/2 with menace
            Permanent arsonist = new Permanent(new ViashinoRunner());
            arsonist.setSummoningSick(false);

            // Opponent has 1 creature — not enough for menace
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new GrizzlyBears()));
            double fewBlockersScore = evaluator.creatureScore(gd, arsonist, player1.getId(), player2.getId());

            // Opponent has 3 creatures — plenty for menace
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new GrizzlyBears()));
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new GrizzlyBears()));
            double manyBlockersScore = evaluator.creatureScore(gd, arsonist, player1.getId(), player2.getId());

            // Menace should score higher when opponent can't provide 2 blockers
            assertThat(fewBlockersScore).isGreaterThan(manyBlockersScore);
        }

        @Test
        @DisplayName("Landwalk creature scores higher when opponent has matching land")
        void landwalkContextAwareScoring() {
            // Bog Wraith: 3/3 with swampwalk
            Permanent bogWraith = new Permanent(new BogWraith());
            bogWraith.setSummoningSick(false);

            // Opponent has a swamp — swampwalk is effectively unblockable
            Permanent swamp = new Permanent(new Swamp());
            swamp.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(swamp);
            double withSwampScore = evaluator.creatureScore(gd, bogWraith, player1.getId(), player2.getId());

            // Opponent has no swamps
            gd.playerBattlefields.get(player2.getId()).clear();
            double withoutSwampScore = evaluator.creatureScore(gd, bogWraith, player1.getId(), player2.getId());

            // Swampwalk should score higher when opponent has a swamp
            assertThat(withSwampScore).isGreaterThan(withoutSwampScore);
        }

        @Test
        @DisplayName("Lifelink creature scores higher when controller is in danger")
        void lifelinkScalesWithDangerLevel() {
            // Baneslayer Angel: 5/5 flying, first strike, lifelink
            Permanent angel = createReadyCreature(new BaneslayerAngel());

            // Opponent has a 6/4 creature threatening the AI
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new CrawWurm()));

            // AI at 5 life — high danger (6/5 = 1.2 multiplier)
            gd.playerLifeTotals.put(player1.getId(), 5);
            double dangerScore = evaluator.creatureScore(gd, angel, player1.getId(), player2.getId());

            // AI at 20 life — low danger (6/20 = 0.3 multiplier)
            gd.playerLifeTotals.put(player1.getId(), 20);
            double safeScore = evaluator.creatureScore(gd, angel, player1.getId(), player2.getId());

            // Lifelink should be worth more when under pressure
            assertThat(dangerScore).isGreaterThan(safeScore);
        }

        @Test
        @DisplayName("Lifelink creature card score scales with danger for hand evaluation")
        void lifelinkCardScoreScalesWithDanger() {
            // Opponent has a 6/4 creature
            gd.playerBattlefields.get(player2.getId()).add(createReadyCreature(new CrawWurm()));

            // AI at 5 life — high danger
            gd.playerLifeTotals.put(player1.getId(), 5);
            double dangerScore = evaluator.creatureCardScore(gd, new BaneslayerAngel(), player1.getId());

            // AI at 20 life — low danger
            gd.playerLifeTotals.put(player1.getId(), 20);
            double safeScore = evaluator.creatureCardScore(gd, new BaneslayerAngel(), player1.getId());

            assertThat(dangerScore).isGreaterThan(safeScore);
        }

        private Permanent createReadyCreature(com.github.laxika.magicalvibes.model.Card card) {
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            return perm;
        }
    }

    // ===== Sacrifice cost scoring =====

    @Nested
    @DisplayName("Sacrifice cost scoring")
    class SacrificeCostScoring {

        @BeforeEach
        void clearBoards() {
            gd.playerBattlefields.get(player1.getId()).clear();
            gd.playerBattlefields.get(player2.getId()).clear();
        }

        @Test
        @DisplayName("Token creature has much lower sacrifice cost than non-token")
        void tokenCreatureCheapToSacrifice() {
            Permanent bears = createReadyCreature(new GrizzlyBears());

            Card tokenCard = new Card();
            tokenCard.setName("Saproling");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setPower(1);
            tokenCard.setToughness(1);
            tokenCard.setToken(true);
            Permanent token = createReadyCreature(tokenCard);

            double bearsCost = evaluator.sacrificeCost(gd, bears, player1.getId(), player2.getId());
            double tokenCost = evaluator.sacrificeCost(gd, token, player1.getId(), player2.getId());

            // Token should be much cheaper to sacrifice
            assertThat(tokenCost).isLessThan(bearsCost * 0.5);
        }

        @Test
        @DisplayName("Pacified creature has much lower sacrifice cost")
        void pacifiedCreatureCheapToSacrifice() {
            Permanent bears = createReadyCreature(new GrizzlyBears());
            gd.playerBattlefields.get(player1.getId()).add(bears);

            // Attach a Pacifism aura to the creature
            Permanent pacifism = new Permanent(new Pacifism());
            pacifism.setAttachedTo(bears.getId());
            gd.playerBattlefields.get(player2.getId()).add(pacifism);

            Permanent healthyBears = createReadyCreature(new GrizzlyBears());

            double pacifiedCost = evaluator.sacrificeCost(gd, bears, player1.getId(), player2.getId());
            double healthyCost = evaluator.sacrificeCost(gd, healthyBears, player1.getId(), player2.getId());

            // Pacified creature should be much cheaper to sacrifice
            assertThat(pacifiedCost).isLessThan(healthyCost * 0.5);
        }

        @Test
        @DisplayName("Creature about to die from damage is near-free to sacrifice")
        void dyingCreatureNearFreeToSacrifice() {
            Permanent bears = createReadyCreature(new GrizzlyBears());
            bears.setMarkedDamage(2); // 2/2 with 2 damage = about to die

            double cost = evaluator.sacrificeCost(gd, bears, player1.getId(), player2.getId());

            assertThat(cost).isEqualTo(0.5);
        }

        @Test
        @DisplayName("0-power creature is cheap to sacrifice")
        void zeroPowerCreatureCheapToSacrifice() {
            Permanent wall = createReadyCreature(new WallOfWood());
            Permanent bears = createReadyCreature(new GrizzlyBears());

            double wallCost = evaluator.sacrificeCost(gd, wall, player1.getId(), player2.getId());
            double bearsCost = evaluator.sacrificeCost(gd, bears, player1.getId(), player2.getId());

            // 0-power creature should be cheaper to sacrifice
            assertThat(wallCost).isLessThan(bearsCost);
        }

        @Test
        @DisplayName("Lord is more expensive to sacrifice than vanilla creature")
        void lordMoreExpensiveToSacrifice() {
            // Benalish Marshal: 3/3 that gives +1/+1 to other creatures
            Permanent marshal = harness.addToBattlefieldAndReturn(player1, new BenalishMarshal());

            // Add some creatures for the lord to pump
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            // Air Elemental is a 4/4 — bigger body but no lord effect
            Permanent airElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

            double marshalCost = evaluator.sacrificeCost(gd, marshal, player1.getId(), player2.getId());
            double airElementalCost = evaluator.sacrificeCost(gd, airElemental, player1.getId(), player2.getId());

            // Lord pumping 4 other creatures (3 bears + air elemental) should be more expensive
            assertThat(marshalCost).isGreaterThan(airElementalCost);
        }

        @Test
        @DisplayName("bestSacrificeCost picks the cheapest creature")
        void bestSacrificeCostPicksCheapest() {
            Card tokenCard = new Card();
            tokenCard.setName("Plant");
            tokenCard.setType(CardType.CREATURE);
            tokenCard.setPower(0);
            tokenCard.setToughness(1);
            tokenCard.setToken(true);
            Permanent token = createReadyCreature(tokenCard);

            Permanent airElemental = createReadyCreature(new AirElemental());

            java.util.List<Permanent> creatures = java.util.List.of(airElemental, token);

            double bestCost = evaluator.bestSacrificeCost(gd, creatures, player1.getId(), player2.getId());
            double tokenCost = evaluator.sacrificeCost(gd, token, player1.getId(), player2.getId());

            // bestSacrificeCost should return the token's cost (the cheapest)
            assertThat(bestCost).isEqualTo(tokenCost);
        }

        @Test
        @DisplayName("bestSacrificeCost returns 0 for empty list")
        void bestSacrificeCostEmptyList() {
            double cost = evaluator.bestSacrificeCost(gd, java.util.List.of(), player1.getId(), player2.getId());
            assertThat(cost).isEqualTo(0.0);
        }

        private Permanent createReadyCreature(Card card) {
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            return perm;
        }
    }
}
