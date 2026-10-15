package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShangChiAndTheTenRings.class, Forest.class})
class ShangChiAndTheTenRingsTest extends BaseCardTest {

    @Test
    @DisplayName("The tenth +1/+1 counter draws five cards and gains 5 life")
    void tenthCounterTriggersDrawAndLifeGain() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        shangChi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));

        advanceToDraw(player1);
        harness.passBothPriorities(); // The draw puts the tenth counter on Shang-Chi.
        harness.passBothPriorities(); // The tenth-counter trigger draws five and gains 5 life.
        resolveAllTriggers(); // Resolve the five draw triggers caused by the payoff.

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Each card in a multi-card draw puts one counter on Shang-Chi")
    void multiCardDrawAddsOneCounterPerCard() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCards(gd, player1.getId(), 3));
        resolveAllTriggers();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An opponent's draw does not put a counter on Shang-Chi")
    void opponentDrawDoesNotAddCounter() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        harness.setLibrary(player2, List.of(new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        resolveAllTriggers();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Adding an eleventh counter does not trigger the payoff again")
    void eleventhCounterDoesNotTriggerPayoff() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        shangChi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        resolveAllTriggers();

        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(11);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The tenth-counter payoff resolves even if the counters are removed in response")
    void payoffResolvesAfterCountersAreRemoved() {
        Permanent shangChi = prepareTenthCounterTrigger();
        shangChi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertLife(player1, 25);
        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("The tenth-counter payoff resolves even if Shang-Chi leaves the battlefield")
    void payoffResolvesAfterShangChiLeavesBattlefield() {
        Permanent shangChi = prepareTenthCounterTrigger();
        gd.playerBattlefields.get(player1.getId()).remove(shangChi);
        gd.playerGraveyards.get(player1.getId()).add(shangChi.getCard());

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.assertLife(player1, 25);
    }

    private Permanent prepareTenthCounterTrigger() {
        Permanent shangChi = harness.addToBattlefieldAndReturn(player1, new ShangChiAndTheTenRings());
        shangChi.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 9);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        assertThat(shangChi.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(10);
        assertThat(gd.stack).hasSize(1);
        return shangChi;
    }
    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.passUntil(activePlayer, TurnStep.DRAW);
    }
}
