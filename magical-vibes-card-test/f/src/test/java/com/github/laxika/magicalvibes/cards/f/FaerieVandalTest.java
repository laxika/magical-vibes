package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaerieVandal.class})
class FaerieVandalTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Faerie Vandal")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());
        harness.setLibrary(player1, List.of(new FaerieVandal(), new FaerieVandal(), new FaerieVandal()));

        draw(player1.getId());
        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the controller's draws count, including during an opponent's turn")
    void triggersOnControllerSecondDrawDuringOpponentTurn() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());
        harness.forceActivePlayer(player2);
        harness.setLibrary(player1, List.of(new FaerieVandal(), new FaerieVandal()));
        harness.setLibrary(player2, List.of(new FaerieVandal(), new FaerieVandal()));

        draw(player2.getId());
        draw(player2.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1.getId());
        assertThat(gd.stack).isEmpty();
        draw(player1.getId());
        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A draw before Faerie Vandal enters still counts toward the second draw")
    void countsFirstDrawBeforeEnteringBattlefield() {
        harness.setLibrary(player1, List.of(new FaerieVandal(), new FaerieVandal()));
        draw(player1.getId());
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());

        draw(player1.getId());
        resolveAllTriggers();

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger on the third draw")
    void doesNotTriggerIfSecondDrawAlreadyHappened() {
        harness.setLibrary(player1, List.of(new FaerieVandal(), new FaerieVandal(), new FaerieVandal()));
        draw(player1.getId());
        draw(player1.getId());
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());

        draw(player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each Faerie Vandal puts its counter on itself")
    void eachVandalGetsItsOwnCounter() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new FaerieVandal());
        harness.setLibrary(player1, List.of(new FaerieVandal(), new FaerieVandal()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
