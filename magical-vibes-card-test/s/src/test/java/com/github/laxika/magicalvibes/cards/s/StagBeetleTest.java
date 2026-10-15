package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StagBeetle.class, SpinedBasher.class, Island.class})
class StagBeetleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter for each other creature on the battlefield")
    void entersWithCountersForOtherCreatures() {
        harness.addToBattlefield(player1, new SpinedBasher());
        harness.addToBattlefield(player2, new SpinedBasher());
        harness.addToBattlefield(player1, new Island());

        harness.castFromHand(player1, new StagBeetle(), "{3}{G}{G}");
        harness.passBothPriorities();

        Permanent stagBeetle = findPermanent(player1, "Stag Beetle");
        assertThat(stagBeetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Dies immediately when no other creatures are on the battlefield")
    void diesWithoutOtherCreatures() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new StagBeetle(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Stag Beetle");
        harness.assertInGraveyard(player1, "Stag Beetle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts creatures when it enters and keeps that counter count afterward")
    void countsAtEntryRatherThanAtCasting() {
        harness.addToBattlefield(player1, new SpinedBasher());
        harness.castFromHand(player1, new StagBeetle(), "{3}{G}{G}");
        harness.addToBattlefield(player2, new SpinedBasher());
        harness.passBothPriorities();

        Permanent stagBeetle = findPermanent(player1, "Stag Beetle");
        assertThat(stagBeetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        harness.addToBattlefield(player2, new SpinedBasher());
        harness.runStateBasedActions();

        assertThat(stagBeetle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
