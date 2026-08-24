package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DireStrainBrawler;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TirelessHauler.class, DireStrainBrawler.class})
class TirelessHaulerTest extends BaseCardTest {

    @Test
    void transformsToDireStrainBrawlerWhenItBecomesNight() {
        gd.dayNight = DayNight.DAY;
        Permanent hauler = harness.addToBattlefieldAndReturn(player1, new TirelessHauler());

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(hauler.getCard()).isInstanceOf(DireStrainBrawler.class);
    }

    @Test
    void transformsToTirelessHaulerWhenItBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        Permanent brawler = harness.addToBattlefieldAndReturn(player1, new TirelessHauler());

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(brawler.getCard()).isInstanceOf(TirelessHauler.class);
    }

    private void advanceToUntap(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
