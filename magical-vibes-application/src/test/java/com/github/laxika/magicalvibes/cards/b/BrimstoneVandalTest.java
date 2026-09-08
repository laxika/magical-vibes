package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BrimstoneVandal.class)
class BrimstoneVandalTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void dealsDamageToEachOpponentWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new BrimstoneVandal());

        makeItNight();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsDamageToEachOpponentWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        gd.spellsCastLastTurn.put(player2.getId(), 2);
        harness.addToBattlefield(player1, new BrimstoneVandal());

        makeItDay();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void makeItNight() {
        harness.performUntapStep(player1);
        harness.passBothPriorities();
    }

    private void makeItDay() {
        harness.performUntapStep(player2);
        harness.passBothPriorities();
    }
}
