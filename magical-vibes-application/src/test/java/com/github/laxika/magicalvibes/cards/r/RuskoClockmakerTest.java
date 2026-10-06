package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MidnightClock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuskoClockmaker.class, MidnightClock.class, DarkRitual.class, GrizzlyBears.class})
class RuskoClockmakerTest extends BaseCardTest {

    @Test
    void entersAndConjuresMidnightClock() {
        harness.castFromHand(player1, new RuskoClockmaker(), "{2}{U}{B}");
        resolveAllTriggers();

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
        harness.castFromHand(player1, new DarkRitual(), "{B}");
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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void noncreatureSpellDrainsLifeWithoutAnyClock() {
        addCreatureReady(player1, new RuskoClockmaker());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void onlyControlledClocksReceiveCounters() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent ownClock = harness.addToBattlefieldAndReturn(player1, new MidnightClock());
        Permanent opposingClock = harness.addToBattlefieldAndReturn(player2, new MidnightClock());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(ownClock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
        assertThat(opposingClock.getCounterCount(CounterType.HOUR)).isZero();
    }

    @Test
    void opposingNoncreatureSpellDoesNotTriggerRusko() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new MidnightClock());

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void castingClockTriggersBeforeNewClockEntersBattlefield() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent existingClock = harness.addToBattlefieldAndReturn(player1, new MidnightClock());
        MidnightClock spell = new MidnightClock();

        harness.castFromHand(player1, spell, "{2}{U}");
        harness.passBothPriorities();

        assertThat(existingClock.getCounterCount(CounterType.HOUR)).isEqualTo(1);
        assertThat(countPermanents(player1, "Midnight Clock")).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Midnight Clock"))
                .filteredOn(p -> p.getCard().getId().equals(spell.getId()))
                .singleElement()
                .satisfies(p -> assertThat(p.getCounterCount(CounterType.HOUR)).isZero());
    }

    @Test
    void ruskoCounterTriggersMidnightClocksTwelfthHourAbility() {
        addCreatureReady(player1, new RuskoClockmaker());
        Permanent clock = harness.addToBattlefieldAndReturn(player1, new MidnightClock());
        clock.setCounterCount(CounterType.HOUR, 11);
        harness.setLibrary(player1, IntStream.range(0, 8)
                .mapToObj(i -> new DarkRitual()).toList());

        harness.castFromHand(player1, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        assertThat(clock.getCounterCount(CounterType.HOUR)).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Midnight Clock")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(clock.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }
}
