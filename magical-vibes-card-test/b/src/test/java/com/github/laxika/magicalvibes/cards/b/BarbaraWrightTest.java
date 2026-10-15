package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.h.HistoryOfBenalia;
import com.github.laxika.magicalvibes.cards.c.Clockspinning;
import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BarbaraWright.class, HistoryOfBenalia.class, Clockspinning.class, DoublingSeason.class})
class BarbaraWrightTest extends BaseCardTest {

    @Test
    void controlledSagaCanReadAheadToChapterThree() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "3");

        Permanent saga = findPermanent(player1, "History of Benalia");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> {
                    assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
                    assertThat(entry.getDescription()).contains("chapter III");
                });
    }

    @Test
    void readAheadDoesNotAffectOpponentSagas() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();

        Permanent saga = findPermanent(player2, "History of Benalia");
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).contains("chapter I"));
    }

    @Test
    void doubledEntryCountersTriggerOnlyTheActualChapter() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");

        assertThat(findPermanent(player1, "History of Benalia").getCounterCount(CounterType.LORE))
                .isEqualTo(2);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).contains("chapter II"));
    }

    @Test
    void addingMultipleLoreCountersOnEntryTurnTriggersOnlyFinalChapter() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();
        Permanent saga = findPermanent(player1, "History of Benalia");
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, saga.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "lore counters");
        harness.handleListChoice(player1, "ADD");

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).contains("chapter III"));
    }

    @Test
    void choosingFirstChapterResolvesNormally() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "1");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "History of Benalia").getCounterCount(CounterType.LORE))
                .isEqualTo(1);
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
    }

    @Test
    void choosingMiddleChapterSkipsFirstChapter() {
        harness.addToBattlefield(player1, new BarbaraWright());
        harness.castFromHand(player1, new HistoryOfBenalia(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "2");

        assertThat(findPermanent(player1, "History of Benalia").getCounterCount(CounterType.LORE))
                .isEqualTo(2);
        assertThat(gd.stack).singleElement()
                .satisfies(entry -> assertThat(entry.getDescription()).contains("chapter II"));
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Knight")).isEqualTo(1);
    }
}
