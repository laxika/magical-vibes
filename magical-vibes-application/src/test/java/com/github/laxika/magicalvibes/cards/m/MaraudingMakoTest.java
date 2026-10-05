package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DangerousWager;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaraudingMako.class, DangerousWager.class, GrizzlyBears.class, Peek.class})
class MaraudingMakoTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling puts one +1/+1 counter on Marauding Mako")
    void cyclingPutsOneCounterOnMako() {
        Permanent mako = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.setHand(player1, List.of(new MaraudingMako()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(mako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Marauding Mako");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Discarding two cards in one event puts two +1/+1 counters on Marauding Mako")
    void discardingTwoCardsInOneEventPutsTwoCountersOnMako() {
        Permanent mako = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.setHand(player1, new ArrayList<>(List.of(
                new DangerousWager(), new GrizzlyBears(), new Peek())));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(mako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void opponentCyclingDoesNotPutCountersOnMako() {
        Permanent mako = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.setHand(player2, List.of(new MaraudingMako()));
        harness.setLibrary(player2, List.of(new MaraudingMako()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(mako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player2, "Marauding Mako");
        harness.assertInHand(player2, "Marauding Mako");
    }

    @Test
    void cyclingWithoutBattlefieldMakoOnlyDrawsACard() {
        harness.setHand(player1, List.of(new MaraudingMako()));
        harness.setLibrary(player1, List.of(new MaraudingMako()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Marauding Mako");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();

        harness.assertInHand(player1, "Marauding Mako");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void separateCyclingEventsEachPutACounterOnEveryControlledMako() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.setHand(player1, List.of(new MaraudingMako(), new MaraudingMako()));
        harness.setLibrary(player1, List.of(new MaraudingMako(), new MaraudingMako()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void discardingAnEmptyHandDoesNotTriggerMako() {
        Permanent mako = harness.addToBattlefieldAndReturn(player1, new MaraudingMako());
        harness.setHand(player1, List.of(new DangerousWager()));
        harness.setLibrary(player1, List.of(new MaraudingMako(), new MaraudingMako()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(mako.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
