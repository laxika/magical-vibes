package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.Compulsion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingFox.class, Censor.class, Compulsion.class, GrizzlyBears.class})
class FlourishingFoxTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling another card puts a +1/+1 counter on Flourishing Fox")
    void cyclingAnotherCardAddsCounter() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A normal discard does not trigger Flourishing Fox")
    void normalDiscardDoesNotAddCounter() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.addToBattlefield(player1, new Compulsion());
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling Flourishing Fox discards it and draws without triggering itself")
    void cyclingFoxFromHandDrawsWithoutSelfTrigger() {
        FlourishingFox cycledFox = new FlourishingFox();
        FlourishingFox drawnFox = new FlourishingFox();
        harness.setHand(player1, List.of(cycledFox));
        harness.setLibrary(player1, List.of(drawnFox));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cycledFox);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFox);
    }

    @Test
    @DisplayName("The counter trigger resolves before the cycling draw")
    void counterIsAddedBeforeCyclingDraw() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        FlourishingFox drawnFox = new FlourishingFox();
        harness.setHand(player1, List.of(new FlourishingFox()));
        harness.setLibrary(player1, List.of(drawnFox));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbility(player1, 0, null);

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnFox);
    }

    @Test
    @DisplayName("Cycling by an opponent does not put a counter on your Fox")
    void opponentsCyclingDoesNotAddCounter() {
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        FlourishingFox drawnFox = new FlourishingFox();
        harness.setHand(player2, List.of(new FlourishingFox()));
        harness.setLibrary(player2, List.of(drawnFox));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.activateHandAbility(player2, 0, null);
        resolveAllTriggers();

        assertThat(fox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnFox);
    }

    @Test
    @DisplayName("Each Fox gets a counter for each card its controller cycles")
    void multipleFoxesTriggerForEveryCycle() {
        Permanent firstFox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        Permanent secondFox = harness.addToBattlefieldAndReturn(player1, new FlourishingFox());
        harness.setHand(player1, List.of(new FlourishingFox(), new FlourishingFox()));
        harness.setLibrary(player1, List.of(new FlourishingFox(), new FlourishingFox()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(firstFox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondFox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        assertThat(firstFox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondFox.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }
}
