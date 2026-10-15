package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirmamentSage.class, Forest.class})
class FirmamentSageTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new FirmamentSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void drawsWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new FirmamentSage());
        Card drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        advanceToNextUpkeepAndResolveTriggers();

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
        gd.recordSpellCast(player1.getId(), new FirmamentSage());
        gd.recordSpellCast(player1.getId(), new FirmamentSage());

        advanceToNextUpkeepAndResolveTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void enteringAtNightDoesNotMakeItDayOrDraw() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new FirmamentSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card undrawn = new Forest();
        harness.setLibrary(player1, List.of(undrawn));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void enteringDuringDayDoesNotDraw() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new FirmamentSage()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentsSpellsDoNotMakeNightBecomeDay() {
        gd.dayNight = DayNight.NIGHT;
        harness.addToBattlefield(player1, new FirmamentSage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        gd.recordSpellCast(player2.getId(), new FirmamentSage());
        gd.recordSpellCast(player2.getId(), new FirmamentSage());

        advanceToNextUpkeepAndResolveTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachSageDrawsForItsController() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new FirmamentSage());
        harness.addToBattlefield(player1, new FirmamentSage());
        harness.addToBattlefield(player2, new FirmamentSage());
        Card first = new Forest();
        Card second = new Forest();
        Card opponentDraw = new Forest();
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.setLibrary(player2, List.of(opponentDraw));

        advanceToNextUpkeepAndResolveTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentDraw);
    }

    private void advanceToNextUpkeepAndResolveTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        for (int i = 0; i < 3 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.stack).isEmpty();
    }
}
