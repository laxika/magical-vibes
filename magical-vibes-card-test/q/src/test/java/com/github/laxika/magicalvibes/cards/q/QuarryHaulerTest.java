package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuarryHauler.class, Colossapede.class, Mountain.class})
class QuarryHaulerTest extends BaseCardTest {

    private void setup() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new QuarryHauler()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("ETB: add one more counter of a kind the target has")
    void addsCounter() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities(); // resolve creature spell — ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB — awaits add/remove choice
        harness.handleListChoice(player1, "ADD");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB: remove one counter of a kind the target has")
    void removesCounter() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB: an independent add/remove decision is made for each kind of counter")
    void adjustsEachKindIndependently() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        // CHARGE precedes PLUS_ONE_PLUS_ONE in the counter-kind order, so it is prompted first.
        bears.setCounterCount(CounterType.CHARGE, 1);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE"); // charge counter
        harness.handleListChoice(player1, "ADD");    // +1/+1 counters

        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB: a target with no counters does nothing and asks for no choice")
    void noCountersNoChoice() {
        setup();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new Colossapede());

        harness.castCreature(player1, 0, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB can remove the last counter from a land you control")
    void removesLastCounterFromOwnLand() {
        setup();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        land.setCounterCount(CounterType.CHARGE, 1);

        harness.castCreature(player1, 0, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "REMOVE");

        assertThat(land.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Mountain");
    }

    @Test
    @DisplayName("ETB uses counter kinds present at resolution, including newly added kinds")
    void includesCountersAddedBeforeResolution() {
        setup();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Colossapede());

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ADD");

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB ignores counter kinds removed before resolution")
    void ignoresCountersRemovedBeforeResolution() {
        setup();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        target.setCounterCount(CounterType.CHARGE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB does not affect a target that left the battlefield")
    void targetLeavesBeforeResolution() {
        setup();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Colossapede());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, target));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Colossapede");
        harness.assertNotOnBattlefield(player2, "Colossapede");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
