package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({AtlanteanCavalry.class})
class AtlanteanCavalryTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller draws their second card of the turn")
    void putsCounterOnSecondDraw() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry(), new AtlanteanCavalry()));

        draw(player1);
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();

        draw(player1);
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        draw(player1);
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when an opponent draws their second card")
    void doesNotTriggerForOpponentDraws() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        harness.setLibrary(player2, List.of(new AtlanteanCavalry(), new AtlanteanCavalry()));

        draw(player2);
        draw(player2);

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Triggers for its controller's second draw on an opponent's turn")
    void triggersOnOpponentTurn() {
        harness.forceActivePlayer(player2);
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry()));

        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts a first draw made before this creature entered the battlefield")
    void countsDrawBeforeEntering() {
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry()));
        draw(player1);
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());

        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Entering after the second draw does not trigger retroactively or on the third draw")
    void doesNotTriggerAfterSecondDrawAlreadyOccurred() {
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry(),
                new AtlanteanCavalry()));
        draw(player1);
        draw(player1);
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());

        draw(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each copy puts a counter only on itself")
    void multipleCopiesTriggerIndependently() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry()));

        draw(player1);
        draw(player1);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can trigger again on the next turn after draw counts reset")
    void triggersAgainOnNextTurn() {
        Permanent cavalry = harness.addToBattlefieldAndReturn(player1, new AtlanteanCavalry());
        harness.setLibrary(player1, List.of(new AtlanteanCavalry(), new AtlanteanCavalry(),
                new AtlanteanCavalry(), new AtlanteanCavalry()));
        harness.setLibrary(player2, List.of(new AtlanteanCavalry(), new AtlanteanCavalry()));
        draw(player1);
        draw(player1);
        resolveAllTriggers();
        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        draw(player1);
        assertThat(gd.stack).isEmpty();
        draw(player1);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(cavalry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

}
