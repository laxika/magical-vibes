package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DayOfJudgment;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.Slagstorm;
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
class GamePhaseTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();

        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
    }

    @Nested
    @DisplayName("GamePhase.determine()")
    class DeterminePhase {

        @Test
        @DisplayName("0 lands returns EARLY")
        void noLandsIsEarly() {
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.EARLY);
        }

        @Test
        @DisplayName("2 lands returns EARLY")
        void twoLandsIsEarly() {
            addLands(player1, 2);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.EARLY);
        }

        @Test
        @DisplayName("3 lands returns EARLY")
        void threeLandsIsEarly() {
            addLands(player1, 3);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.EARLY);
        }

        @Test
        @DisplayName("4 lands returns MID")
        void fourLandsIsMid() {
            addLands(player1, 4);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.MID);
        }

        @Test
        @DisplayName("5 lands returns MID")
        void fiveLandsIsMid() {
            addLands(player1, 5);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.MID);
        }

        @Test
        @DisplayName("6 lands returns LATE")
        void sixLandsIsLate() {
            addLands(player1, 6);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.LATE);
        }

        @Test
        @DisplayName("Mana dorks count as mana sources")
        void manaDorksCountAsSources() {
            addLands(player1, 2);
            // Llanowar Elves has a tap-for-mana ability, so 2 lands + 2 elves = 4 sources = MID
            for (int i = 0; i < 2; i++) {
                Permanent elf = new Permanent(new LlanowarElves());
                elf.setSummoningSick(false);
                gd.playerBattlefields.get(player1.getId()).add(elf);
            }
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.MID);
        }

        @Test
        @DisplayName("Non-mana creatures do not count as mana sources")
        void nonManaCreaturesDontCount() {
            addLands(player1, 2);
            // Grizzly Bears have no mana ability — should still be EARLY (2 sources)
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player1.getId()).add(bears);
            assertThat(GamePhase.determine(gd, player1.getId())).isEqualTo(GamePhase.EARLY);
        }
    }

    @Nested
    @DisplayName("Game phase spell value multipliers")
    class PhaseMultipliers {

        private SpellEvaluator spellEvaluator;

        @BeforeEach
        void setUpEvaluator() {
            BoardEvaluator boardEvaluator = new BoardEvaluator(harness.getGameQueryService());
            spellEvaluator = new SpellEvaluator(harness.getGameQueryService(), boardEvaluator);
        }

        @Test
        @DisplayName("Early game: cheap creature is boosted vs mid game")
        void earlyGameBoostsCheapCreature() {
            addLands(player1, 2); // EARLY

            double earlyValue = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

            assertThat(earlyValue).isGreaterThan(midValue);
        }

        @Test
        @DisplayName("Early game: card draw is discounted vs mid game")
        void earlyGameDiscountsCardDraw() {
            addLands(player1, 2); // EARLY

            double earlyValue = spellEvaluator.estimateSpellValue(gd, new Divination(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new Divination(), player1.getId());

            assertThat(earlyValue).isLessThan(midValue);
        }

        @Test
        @DisplayName("Early game: board wipe is discounted vs mid game")
        void earlyGameDiscountsBoardWipe() {
            // Need creatures on opponent's side for board wipe to have value
            Permanent bears1 = new Permanent(new GrizzlyBears());
            bears1.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears1);
            Permanent bears2 = new Permanent(new GrizzlyBears());
            bears2.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears2);

            addLands(player1, 2); // EARLY

            double earlyValue = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

            assertThat(earlyValue).isLessThan(midValue);
        }

        @Test
        @DisplayName("Late game: small creature is discounted vs mid game")
        void lateGameDiscountsSmallCreature() {
            addLands(player1, 7); // LATE

            double lateValue = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new GrizzlyBears(), player1.getId());

            assertThat(lateValue).isLessThan(midValue);
        }

        @Test
        @DisplayName("Late game: big creature is boosted vs mid game")
        void lateGameBoostsBigCreature() {
            addLands(player1, 7); // LATE

            double lateValue = spellEvaluator.estimateSpellValue(gd, new SerraAngel(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new SerraAngel(), player1.getId());

            assertThat(lateValue).isGreaterThan(midValue);
        }

        @Test
        @DisplayName("Late game: card draw is boosted vs mid game")
        void lateGameBoostsCardDraw() {
            addLands(player1, 7); // LATE

            double lateValue = spellEvaluator.estimateSpellValue(gd, new Divination(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new Divination(), player1.getId());

            assertThat(lateValue).isGreaterThan(midValue);
        }

        @Test
        @DisplayName("Late game: board wipe is boosted vs mid game")
        void lateGameBoostsBoardWipe() {
            Permanent bears1 = new Permanent(new GrizzlyBears());
            bears1.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears1);
            Permanent bears2 = new Permanent(new GrizzlyBears());
            bears2.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears2);

            addLands(player1, 7); // LATE

            double lateValue = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 4); // MID

            double midValue = spellEvaluator.estimateSpellValue(gd, new Pyroclasm(), player1.getId());

            assertThat(lateValue).isGreaterThan(midValue);
        }

        @Test
        @DisplayName("Mid game: multiplier is 1.0 (no adjustment)")
        void midGameNoAdjustment() {
            addLands(player1, 4); // MID

            double multiplier = spellEvaluator.gamePhaseMultiplier(gd, new GrizzlyBears(), player1.getId());

            assertThat(multiplier).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Modal board wipe (Slagstorm) is detected as board wipe")
        void modalBoardWipeDetected() {
            Permanent bears = new Permanent(new GrizzlyBears());
            bears.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(bears);

            addLands(player1, 2); // EARLY
            double earlyMultiplier = spellEvaluator.gamePhaseMultiplier(gd, new Slagstorm(), player1.getId());

            gd.playerBattlefields.get(player1.getId()).clear();
            addLands(player1, 7); // LATE
            double lateMultiplier = spellEvaluator.gamePhaseMultiplier(gd, new Slagstorm(), player1.getId());

            // Early should discount, late should boost
            assertThat(earlyMultiplier).isLessThan(1.0);
            assertThat(lateMultiplier).isGreaterThan(1.0);
        }
    }

    private void addLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent land = new Permanent(new Forest());
            gd.playerBattlefields.get(player.getId()).add(land);
        }
    }
}
