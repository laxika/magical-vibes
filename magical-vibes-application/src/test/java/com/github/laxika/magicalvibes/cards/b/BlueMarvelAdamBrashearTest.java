package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlueMarvelAdamBrashear.class})
class BlueMarvelAdamBrashearTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn puts a +1/+1 counter on Blue Marvel")
    void secondDrawAddsCounterOnlyOnce() {
        Permanent blueMarvel = harness.addToBattlefieldAndReturn(player1, new BlueMarvelAdamBrashear());
        harness.setLibrary(player1, List.of(
                new BlueMarvelAdamBrashear(), new BlueMarvelAdamBrashear(), new BlueMarvelAdamBrashear()));

        draw(player1.getId());
        assertThat(blueMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(blueMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's second draw does not put a counter on Blue Marvel")
    void opponentsSecondDrawDoesNotTrigger() {
        Permanent blueMarvel = harness.addToBattlefieldAndReturn(player1, new BlueMarvelAdamBrashear());
        harness.setLibrary(player2, List.of(new BlueMarvelAdamBrashear(), new BlueMarvelAdamBrashear()));

        draw(player2.getId());
        draw(player2.getId());
        resolveAllTriggers();

        assertThat(blueMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A first draw before Blue Marvel enters still counts toward the second draw")
    void firstDrawBeforeEnteringCounts() {
        harness.setLibrary(player1, List.of(new BlueMarvelAdamBrashear(), new BlueMarvelAdamBrashear()));
        draw(player1.getId());
        Permanent blueMarvel = harness.addToBattlefieldAndReturn(player1, new BlueMarvelAdamBrashear());

        draw(player1.getId());
        resolveAllTriggers();

        assertThat(blueMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller's second draw triggers during an opponent's turn")
    void secondDrawDuringOpponentsTurnTriggers() {
        harness.forceActivePlayer(player2);
        Permanent blueMarvel = harness.addToBattlefieldAndReturn(player1, new BlueMarvelAdamBrashear());
        harness.setLibrary(player1, List.of(new BlueMarvelAdamBrashear(), new BlueMarvelAdamBrashear()));

        draw(player1.getId());
        draw(player1.getId());
        resolveAllTriggers();

        assertThat(blueMarvel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void draw(UUID playerId) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, playerId));
    }
}
