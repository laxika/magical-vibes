package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BirdAdmirer.class, WingShredder.class, BenevolentGeist.class})
class BirdAdmirerTest extends BaseCardTest {

    @Test
    void becomesDayWhenItEntersWithoutADesignation() {
        Permanent admirer = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(admirer.isTransformed()).isFalse();
        assertThat(admirer.getCard()).isInstanceOf(BirdAdmirer.class);
    }

    @Test
    void entersAsWingShredderDuringNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent admirer = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());

        assertThat(admirer.isTransformed()).isTrue();
        assertThat(admirer.getCard()).isInstanceOf(WingShredder.class);
    }

    @Test
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent admirer = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());

        gd.spellsCastLastTurn.clear();
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(admirer.isTransformed()).isTrue();
        assertThat(admirer.getCard()).isInstanceOf(WingShredder.class);

        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(admirer.isTransformed()).isFalse();
        assertThat(admirer.getCard()).isInstanceOf(BirdAdmirer.class);
    }

    @Test
    void staysDayWhenPreviousActivePlayerCastOneSpell() {
        gd.dayNight = DayNight.DAY;
        Permanent admirer = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(admirer.isTransformed()).isFalse();
    }

    @Test
    void spellsCastByNonactivePlayerDoNotMakeItDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent admirer = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        gd.spellsCastLastTurn.put(player2.getId(), 1);

        harness.performUntapStep(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(admirer.isTransformed()).isTrue();
    }

    @Test
    void bothFacesCanBlockFlyingCreatures() {
        gd.dayNight = DayNight.DAY;
        Permanent front = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());
        Permanent flyer = addCreatureReady(player2, new BenevolentGeist());

        assertThat(bls.canBlockAttacker(gd, front, flyer,
                gd.playerBattlefields.get(player1.getId()))).isTrue();

        gd.dayNight = DayNight.NIGHT;
        Permanent back = harness.enterBattlefieldAndReturn(player1, new BirdAdmirer());

        assertThat(bls.canBlockAttacker(gd, back, flyer,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }
}
