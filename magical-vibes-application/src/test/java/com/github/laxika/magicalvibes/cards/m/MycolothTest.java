package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BantCharm;
import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mycoloth.class, CylianElf.class, BantCharm.class})
class MycolothTest extends BaseCardTest {

    private List<Permanent> saprolings(Player player) {
        return findPermanents(player, "Saproling");
    }

    @Test
    @DisplayName("Devouring two creatures enters with four counters, then upkeep makes four Saprolings")
    void devourThenUpkeepTokens() {
        Permanent fodderA = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        Permanent fodderB = harness.addToBattlefieldAndReturn(player1, new CylianElf());

        harness.castFromHand(player1, new Mycoloth(), "{3}{G}{G}");
        harness.passBothPriorities(); // resolve creature spell -> devour choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(fodderA.getId(), fodderB.getId()));

        Permanent mycoloth = findPermanent(player1, "Mycoloth");
        assertThat(mycoloth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(saprolings(player1)).hasSize(4);
    }

    @Test
    @DisplayName("With no +1/+1 counters, no Saprolings are created on upkeep")
    void noCountersNoTokens() {
        harness.addToBattlefield(player1, new Mycoloth());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(saprolings(player1)).isEmpty();
    }

    @Test
    @DisplayName("Creates one Saproling per +1/+1 counter on upkeep")
    void oneSaprolingPerCounter() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(saprolings(player1)).hasSize(3);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player2); // opponent's upkeep

        assertThat(saprolings(player1)).isEmpty();
    }

    @Test
    @DisplayName("Devour may be declined even when creatures are available")
    void mayDeclineDevour() {
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.castFromHand(player1, new Mycoloth(), "{3}{G}{G}");
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fodder);
        assertThat(findPermanent(player1, "Mycoloth").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Upkeep counts counters at resolution and ignores other counter types")
    void countsCurrentPlusOneCountersAtResolution() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        mycoloth.setCounterCount(CounterType.CHARGE, 5);
        advanceToUpkeep(player1);

        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(saprolings(player1)).hasSize(4);
        assertThat(saprolings(player2)).isEmpty();
    }

    @Test
    @DisplayName("Upkeep uses last known counters when Mycoloth leaves in response")
    void usesLastKnownCountersAfterLeavingBattlefield() {
        Permanent mycoloth = harness.addToBattlefieldAndReturn(player1, new Mycoloth());
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        advanceToUpkeep(player1);
        mycoloth.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player2, List.of(new BantCharm()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, 1, mycoloth.getId());
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Mycoloth")).isEmpty();

        harness.passBothPriorities();

        assertThat(saprolings(player1)).hasSize(4);
        assertThat(saprolings(player2)).isEmpty();
    }
}
