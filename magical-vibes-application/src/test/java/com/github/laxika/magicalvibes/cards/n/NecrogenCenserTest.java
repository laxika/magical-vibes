package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({NecrogenCenser.class})
class NecrogenCenserTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 2 charge counters")
    void entersWithTwoChargeCounters() {
        harness.setHand(player1, List.of(new NecrogenCenser()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent censer = findPermanent(player1, "Necrogen Censer");
        assertThat(censer.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating ability removes a charge counter and target player loses 2 life")
    void activateRemovesCounterAndTargetLosesLife() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 2);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(censer.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 2);
    }

    @Test
    @DisplayName("Can activate twice with 2 charge counters (untapping between uses)")
    void canActivateTwice() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 2);

        int initialLife = gd.playerLifeTotals.get(player2.getId());

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        censer.untap();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(censer.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 4);
    }

    @Test
    @DisplayName("Cannot activate with 0 charge counters")
    void cannotActivateWithNoCounters() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target yourself to lose life")
    void canTargetSelf() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 1);

        int initialLife = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(censer.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(initialLife - 2);
    }

    @Test
    @DisplayName("Cannot activate while tapped (requires tap)")
    void cannotActivateWhileTapped() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 2);

        // First activation taps it
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Cannot activate again while tapped
        assertThat(censer.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Tap and counter costs are paid before life loss resolves")
    void costsArePaidAtActivation() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.CHARGE, 1);
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(censer.isTapped()).isTrue();
        assertThat(censer.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 2);
    }

    @Test
    @DisplayName("Counters of another type cannot pay the charge-counter cost")
    void cannotPayWithOtherCounters() {
        Permanent censer = harness.addToBattlefieldAndReturn(player1, new NecrogenCenser());
        censer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(censer.isTapped()).isFalse();
        assertThat(censer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can activate immediately after entering as a noncreature artifact")
    void canActivateOnTurnItEnters() {
        harness.setHand(player1, List.of(new NecrogenCenser()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        Permanent censer = findPermanent(player1, "Necrogen Censer");
        int initialLife = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(censer.isTapped()).isTrue();
        assertThat(censer.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(initialLife - 2);
    }
}
