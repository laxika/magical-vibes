package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirmamentSage.class, Forest.class, GrizzlyBears.class})
class FirmamentSageTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new FirmamentSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
    }

    @Test
    void drawsWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new FirmamentSage());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        makeItNight();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void drawsWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new FirmamentSage());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        makeItDay();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void makeItNight() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void makeItDay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
