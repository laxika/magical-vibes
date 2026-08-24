package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.w.WingShredder;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BirdAdmirer.class, WingShredder.class})
class BirdAdmirerTest extends BaseCardTest {

    @Test
    void becomesDayWhenItEntersWithoutADesignation() {
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new BirdAdmirer());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(admirer.isTransformed()).isFalse();
        assertThat(admirer.getCard()).isInstanceOf(BirdAdmirer.class);
    }

    @Test
    void entersAsWingShredderDuringNight() {
        gd.dayNight = DayNight.NIGHT;
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new BirdAdmirer());

        assertThat(admirer.isTransformed()).isTrue();
        assertThat(admirer.getCard()).isInstanceOf(WingShredder.class);
    }

    @Test
    void transformsWithDayAndNight() {
        gd.dayNight = DayNight.DAY;
        Permanent admirer = harness.addToBattlefieldAndReturn(player1, new BirdAdmirer());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(admirer.isTransformed()).isTrue();
        assertThat(admirer.getCard()).isInstanceOf(WingShredder.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(admirer.isTransformed()).isFalse();
        assertThat(admirer.getCard()).isInstanceOf(BirdAdmirer.class);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
