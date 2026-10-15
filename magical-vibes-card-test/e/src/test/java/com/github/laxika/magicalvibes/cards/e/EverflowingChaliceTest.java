package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EverflowingChalice.class})
class EverflowingChaliceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without charge counters when not kicked")
    void entersWithoutChargeCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new EverflowingChalice()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent chalice = findChalice();
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Enters with one charge counter per multikicker payment")
    void entersWithChargeCountersForEachMultikickerPayment() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}", "{2}"));
        harness.passBothPriorities();

        assertThat(findChalice().getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Multikicker payments require enough mana for every payment")
    void cannotKickTwiceWithOnlyThreeMana() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreatureWithRepeatedCosts(
                player1, 0, List.of("{2}", "{2}")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can tap immediately after entering and produces mana without using the stack")
    void canProduceManaImmediatelyAfterEntering() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        harness.passBothPriorities();

        assertThat(findChalice().getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.activateAbility(player1, 0, null, null);

        assertThat(findChalice().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("A Chalice with no charge counters can tap but adds no mana")
    void tappingWithoutChargeCountersAddsNoMana() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);

        assertThat(findChalice().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Tapping adds one colorless mana per charge counter")
    void tappingAddsColorlessManaPerChargeCounter() {
        Permanent chalice = harness.addToBattlefieldAndReturn(player1, new EverflowingChalice());
        chalice.setSummoningSick(false);
        chalice.setCounterCount(CounterType.CHARGE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
    }

    @Test
    @DisplayName("Mana follows the current charge counters and ignores other counter types")
    void manaUsesCurrentChargeCountersOnly() {
        harness.setHand(player1, List.of(new EverflowingChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{2}"));
        harness.passBothPriorities();
        Permanent chalice = findChalice();
        chalice.setCounterCount(CounterType.CHARGE, 4);
        chalice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(chalice.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    private Permanent findChalice() {
        return findPermanent(player1, "Everflowing Chalice");
    }
}
