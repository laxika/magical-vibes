package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatchfulBlisterzoa.class, GrizzlyBears.class, WrathOfGod.class})
class WatchfulBlisterzoaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with an oil counter")
    void entersWithOilCounter() {
        harness.setHand(player1, List.of(new WatchfulBlisterzoa()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent blisterzoa = findPermanent(player1, "Watchful Blisterzoa");

        assertThat(blisterzoa.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Draws a card for each oil counter on it when it dies")
    void drawsPerOilCounterOnDeath() {
        Permanent blisterzoa = harness.addToBattlefieldAndReturn(player1, new WatchfulBlisterzoa());
        blisterzoa.setCounterCount(CounterType.OIL, 3);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castSorcery(player2, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        harness.assertInGraveyard(player1, "Watchful Blisterzoa");
    }

    @Test
    @DisplayName("Death still triggers with no oil counters and draws zero cards")
    void deathTriggersWithoutOilCounters() {
        Permanent blisterzoa = harness.enterBattlefieldAndReturn(player1, new WatchfulBlisterzoa());
        blisterzoa.setCounterCount(CounterType.OIL, 0);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        blisterzoa.setMarkedDamage(4);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Watchful Blisterzoa");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Death counts only oil counters, ignoring other counter types")
    void deathIgnoresOtherCounterTypes() {
        Permanent blisterzoa = harness.enterBattlefieldAndReturn(player1, new WatchfulBlisterzoa());
        blisterzoa.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        blisterzoa.setCounterCount(CounterType.CHARGE, 3);
        harness.setHand(player1, List.of());
        WatchfulBlisterzoa drawn = new WatchfulBlisterzoa();
        WatchfulBlisterzoa remaining = new WatchfulBlisterzoa();
        harness.setLibrary(player1, List.of(drawn, remaining));
        blisterzoa.setMarkedDamage(6);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        harness.assertInGraveyard(player1, "Watchful Blisterzoa");
    }

    @Test
    @DisplayName("Simultaneous deaths use each creature's oil counters and controller")
    void simultaneousDeathsDrawForEachController() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new WatchfulBlisterzoa());
        Permanent second = harness.enterBattlefieldAndReturn(player2, new WatchfulBlisterzoa());
        second.setCounterCount(CounterType.OIL, 2);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        WatchfulBlisterzoa firstDraw = new WatchfulBlisterzoa();
        WatchfulBlisterzoa secondDraw = new WatchfulBlisterzoa();
        WatchfulBlisterzoa thirdDraw = new WatchfulBlisterzoa();
        harness.setLibrary(player1, List.of(firstDraw, new WatchfulBlisterzoa()));
        harness.setLibrary(player2, List.of(secondDraw, thirdDraw, new WatchfulBlisterzoa()));
        first.setMarkedDamage(4);
        second.setMarkedDamage(4);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(secondDraw, thirdDraw);
        harness.assertInGraveyard(player1, "Watchful Blisterzoa");
        harness.assertInGraveyard(player2, "Watchful Blisterzoa");
    }
}
