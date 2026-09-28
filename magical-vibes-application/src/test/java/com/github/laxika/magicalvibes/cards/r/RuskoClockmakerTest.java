package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MidnightClock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuskoClockmaker.class, MidnightClock.class, DarkRitual.class, GrizzlyBears.class})
class RuskoClockmakerTest extends BaseCardTest {

    @Test
    void entersAndConjuresMidnightClock() {
        harness.setHand(player1, List.of(new RuskoClockmaker()));
        addRuskoMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Midnight Clock"))
                .singleElement()
                .extracting(Permanent::getCard)
                .isInstanceOf(MidnightClock.class);
    }

    @Test
    void noncreatureSpellAddsHourCountersAndDrainsEachOpponent() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent firstClock = addCreatureReady(player1, new MidnightClock());
        Permanent secondClock = addCreatureReady(player1, new MidnightClock());
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(firstClock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
        assertThat(secondClock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void creatureSpellDoesNotTriggerRusko() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent clock = addCreatureReady(player1, new MidnightClock());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void addRuskoMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
