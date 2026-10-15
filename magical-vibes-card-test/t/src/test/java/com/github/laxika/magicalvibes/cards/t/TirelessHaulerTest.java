package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DireStrainBrawler;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessHauler.class, DireStrainBrawler.class})
class TirelessHaulerTest extends BaseCardTest {

    @Test
    void transformsToDireStrainBrawlerWhenItBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent hauler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(hauler.getCard()).isInstanceOf(DireStrainBrawler.class);
    }

    @Test
    void transformsToTirelessHaulerWhenItBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(brawler.getCard()).isInstanceOf(TirelessHauler.class);
    }

    @Test
    void enteringWhenNeitherDayNorNightMakesItDay() {
        gd.dayNight = DayNight.NEITHER;

        Permanent hauler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(hauler.getCard()).isInstanceOf(TirelessHauler.class);
        assertThat(hauler.isTransformed()).isFalse();
    }

    @Test
    void entersTransformedAtNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(brawler.getCard()).isInstanceOf(DireStrainBrawler.class);
        assertThat(brawler.isTransformed()).isTrue();
    }

    @Test
    void oneSpellByPreviousActivePlayerKeepsItDay() {
        gd.dayNight = DayNight.DAY;
        Permanent hauler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(hauler.getCard()).isInstanceOf(TirelessHauler.class);
    }

    @Test
    void oneSpellByPreviousActivePlayerKeepsItNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 1);
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(brawler.getCard()).isInstanceOf(DireStrainBrawler.class);
    }

    @Test
    void spellsByNonactivePlayerDoNotPreventNight() {
        gd.dayNight = DayNight.DAY;
        Permanent hauler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(hauler.getCard()).isInstanceOf(DireStrainBrawler.class);
    }

    @Test
    void previousActivePlayersTwoSpellsMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        gd.previousTurnActivePlayerId = player2.getId();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(brawler.getCard()).isInstanceOf(TirelessHauler.class);
    }

    @Test
    void attackingDuringDayDoesNotTapHauler() {
        gd.dayNight = DayNight.DAY;
        Permanent hauler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        hauler.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(hauler.isAttacking()).isTrue();
        assertThat(hauler.isTapped()).isFalse();
    }

    @Test
    void attackingDuringNightDoesNotTapBrawler() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brawler = harness.enterBattlefieldAndReturn(player1, new TirelessHauler());
        brawler.setSummoningSick(false);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(brawler.isAttacking()).isTrue();
        assertThat(brawler.isTapped()).isFalse();
    }
}
