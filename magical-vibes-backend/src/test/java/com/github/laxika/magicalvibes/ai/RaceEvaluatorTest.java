package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WallOfFrost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
class RaceEvaluatorTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private RaceEvaluator raceEvaluator;
    private GameQueryService gameQueryService;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        gameQueryService = harness.getGameQueryService();
        raceEvaluator = new RaceEvaluator(gameQueryService);

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
    }

    @Nested
    @DisplayName("Clock calculation")
    class ClockCalculation {

        @Test
        @DisplayName("Zero damage per turn = infinite clock")
        void zeroDamageInfiniteClock() {
            assertThat(RaceEvaluator.calculateClock(0, 20)).isEqualTo(Integer.MAX_VALUE);
        }

        @Test
        @DisplayName("Exact lethal in one turn")
        void exactLethalOneTurn() {
            assertThat(RaceEvaluator.calculateClock(20, 20)).isEqualTo(1);
        }

        @Test
        @DisplayName("2 damage per turn vs 20 life = 10 turns")
        void twoDamageVsTwentyLife() {
            assertThat(RaceEvaluator.calculateClock(2, 20)).isEqualTo(10);
        }

        @Test
        @DisplayName("3 damage per turn vs 20 life = 7 turns (ceiling)")
        void threeDamageVsTwentyLife() {
            assertThat(RaceEvaluator.calculateClock(3, 20)).isEqualTo(7);
        }

        @Test
        @DisplayName("5 damage per turn vs 4 life = 1 turn")
        void fiveDamageVsFourLife() {
            assertThat(RaceEvaluator.calculateClock(5, 4)).isEqualTo(1);
        }

        @Test
        @DisplayName("Negative damage = infinite clock")
        void negativeDamageInfiniteClock() {
            assertThat(RaceEvaluator.calculateClock(-1, 20)).isEqualTo(Integer.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Board damage calculation")
    class BoardDamageCalculation {

        @Test
        @DisplayName("Single 2/2 creature deals 2 damage")
        void singleCreatureDamage() {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);

            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(2);
        }

        @Test
        @DisplayName("Multiple creatures sum their power")
        void multipleCreaturesSumDamage() {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);

            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);

            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(6); // 2 + 4
        }

        @Test
        @DisplayName("Summoning sick creature excluded from board damage")
        void summoningSickExcluded() {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(true);
            gd.playerBattlefields.get(player1.getId()).add(bears);

            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(0);
        }

        @Test
        @DisplayName("Tapped creature excluded from board damage")
        void tappedCreatureExcluded() {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            bears.tap();
            gd.playerBattlefields.get(player1.getId()).add(bears);

            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(0);
        }

        @Test
        @DisplayName("Defender creature excluded from board damage")
        void defenderExcluded() {
            Permanent wall = new Permanent(new WallOfFrost());
            wall.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(wall);

            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(0);
        }

        @Test
        @DisplayName("Empty battlefield returns 0 damage")
        void emptyBattlefield() {
            int damage = raceEvaluator.calculateBoardDamage(gd, player1.getId());
            assertThat(damage).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Burn in hand calculation")
    class BurnInHandCalculation {

        @Test
        @DisplayName("Lightning Bolt deals 3 face damage")
        void lightningBoltFaceDamage() {
            int damage = raceEvaluator.getBurnToFaceDamage(new LightningBolt());
            assertThat(damage).isEqualTo(3);
        }

        @Test
        @DisplayName("Shock deals 2 face damage")
        void shockFaceDamage() {
            int damage = raceEvaluator.getBurnToFaceDamage(new Shock());
            assertThat(damage).isEqualTo(2);
        }

        @Test
        @DisplayName("Lava Axe deals 5 face damage (DealDamageToTargetPlayerEffect)")
        void lavaAxeFaceDamage() {
            int damage = raceEvaluator.getBurnToFaceDamage(new LavaAxe());
            assertThat(damage).isEqualTo(5);
        }

        @Test
        @DisplayName("Non-burn card deals 0 face damage")
        void nonBurnCardZeroDamage() {
            int damage = raceEvaluator.getBurnToFaceDamage(new GrizzlyBears());
            assertThat(damage).isEqualTo(0);
        }

        @Test
        @DisplayName("Multiple burn spells sum their damage")
        void multipleBurnSpellsSumDamage() {
            List<Card> burnCards = List.of(new LightningBolt(), new Shock());
            int total = raceEvaluator.calculateBurnInHandDamage(burnCards);
            assertThat(total).isEqualTo(5); // 3 + 2
        }
    }

    @Nested
    @DisplayName("Race state evaluation")
    class RaceStateEvaluation {

        @Test
        @DisplayName("AI with bigger creature wins the race")
        void aiWinningRace() {
            // AI: 4/4 Air Elemental (4 damage/turn, 5-turn clock)
            Permanent aiCreature = new Permanent(new AirElemental());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiCreature);

            // Opponent: 2/2 Grizzly Bears (2 damage/turn, 10-turn clock)
            Permanent oppCreature = new Permanent(new GrizzlyBears());
            oppCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppCreature);

            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), List.of());

            assertThat(state.aiClock()).isEqualTo(5);
            assertThat(state.opponentClock()).isEqualTo(10);
            assertThat(state.aiWinningRace()).isTrue();
            assertThat(state.aiLosingRace()).isFalse();
        }

        @Test
        @DisplayName("AI with smaller creature loses the race")
        void aiLosingRace() {
            // AI: 2/2
            Permanent aiCreature = new Permanent(new GrizzlyBears());
            aiCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(aiCreature);

            // Opponent: 4/4
            Permanent oppCreature = new Permanent(new AirElemental());
            oppCreature.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(oppCreature);

            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), List.of());

            assertThat(state.aiClock()).isEqualTo(10);
            assertThat(state.opponentClock()).isEqualTo(5);
            assertThat(state.aiWinningRace()).isFalse();
            assertThat(state.aiLosingRace()).isTrue();
        }

        @Test
        @DisplayName("Burn lethal detected when burn damage >= opponent life")
        void burnLethalDetected() {
            gd.playerLifeTotals.put(player2.getId(), 5);

            // No creatures needed — just burn
            List<Card> burnCards = List.of(new LightningBolt(), new Shock());

            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), burnCards);

            assertThat(state.burnInHandDamage()).isEqualTo(5);
            assertThat(state.burnLethal()).isTrue();
        }

        @Test
        @DisplayName("Burn not lethal when burn damage < opponent life")
        void burnNotLethalWhenInsufficient() {
            gd.playerLifeTotals.put(player2.getId(), 10);

            List<Card> burnCards = List.of(new Shock()); // only 2 damage

            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), burnCards);

            assertThat(state.burnInHandDamage()).isEqualTo(2);
            assertThat(state.burnLethal()).isFalse();
        }

        @Test
        @DisplayName("No creatures means infinite clock for both")
        void noCreaturesInfiniteClock() {
            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), List.of());

            assertThat(state.aiClock()).isEqualTo(Integer.MAX_VALUE);
            assertThat(state.opponentClock()).isEqualTo(Integer.MAX_VALUE);
            assertThat(state.aiWinningRace()).isFalse();
            assertThat(state.aiLosingRace()).isFalse();
        }

        @Test
        @DisplayName("Board lethal detected on 1-turn clock")
        void boardLethalNextAttack() {
            gd.playerLifeTotals.put(player2.getId(), 4);

            Permanent angel = new Permanent(new SerraAngel());
            angel.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(angel);

            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), List.of());

            assertThat(state.aiBoardDamage()).isEqualTo(4);
            assertThat(state.boardLethalNextAttack()).isTrue();
        }

        @Test
        @DisplayName("Low opponent life with burn + board damage = burn lethal")
        void burnPlusBoardDamage() {
            gd.playerLifeTotals.put(player2.getId(), 3);

            // Burn can deal 3 (Lightning Bolt) — exactly lethal
            List<Card> burnCards = List.of(new LightningBolt());
            RaceEvaluator.RaceState state = raceEvaluator.evaluate(gd, player1.getId(), burnCards);

            assertThat(state.burnLethal()).isTrue();
        }
    }
}
